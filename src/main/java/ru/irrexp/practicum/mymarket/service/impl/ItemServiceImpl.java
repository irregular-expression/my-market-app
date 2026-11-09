package ru.irrexp.practicum.mymarket.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.irrexp.practicum.mymarket.dto.ItemDto;
import ru.irrexp.practicum.mymarket.dto.ItemsPageResponse;
import ru.irrexp.practicum.mymarket.dto.PagingInfo;
import ru.irrexp.practicum.mymarket.entity.CartItem;
import ru.irrexp.practicum.mymarket.entity.Item;
import ru.irrexp.practicum.mymarket.enums.ActionType;
import ru.irrexp.practicum.mymarket.enums.SortType;
import ru.irrexp.practicum.mymarket.exception.IncorrectParamException;
import ru.irrexp.practicum.mymarket.exception.ItemNotFoundException;
import ru.irrexp.practicum.mymarket.mapper.ItemMapper;
import ru.irrexp.practicum.mymarket.repository.CartItemRepository;
import ru.irrexp.practicum.mymarket.repository.ItemRepository;
import ru.irrexp.practicum.mymarket.service.CartService;
import ru.irrexp.practicum.mymarket.service.ItemService;
import ru.irrexp.practicum.mymarket.util.OffsetLimitRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.apache.logging.log4j.util.Strings.isBlank;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private static final Set<Integer> ALLOWED_PAGE_SIZES = Set.of(2, 5, 10, 20, 50, 100);

    private final ItemRepository itemRepository;
    private final CartItemRepository cartItemRepository;
    private final ItemMapper itemMapper;
    private final CartService cartService;

    @Override
    @Transactional(readOnly = true)
    public Mono<ItemsPageResponse> getItems(String search, SortType sort, int pageNumber, int pageSize) {
        validatePaginationParams(pageNumber, pageSize);

        // на одну запись больше, чтобы определить hasNext
        Pageable pageable = OffsetLimitRequest.of(
                pageNumber - 1,
                pageSize + 1,
                (long) (pageNumber - 1) * pageSize,
                toSort(sort));


        return findPage(search, pageable)
                .collectList()
                .flatMap(rows -> buildResponse(rows, search, sort, pageNumber, pageSize));
    }

    private Flux<Item> findPage(String search, Pageable pageable) {
        return isBlank(search)
                ? itemRepository.findAllBy(pageable)
                : itemRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                search, search, pageable);
    }

    private Mono<ItemsPageResponse> buildResponse(List<Item> rows, String search,
                                                  SortType sort, int pageNumber, int pageSize) {
        boolean hasNext = rows.size() > pageSize;
        List<Item> pageContent = hasNext ? rows.subList(0, pageSize) : rows;

        return Flux.fromIterable(pageContent)
                .flatMapSequential(this::mapFromCartIfExisted)
                .collectList()
                .map(itemDtos -> new ItemsPageResponse(
                        groupIntoRowsOfThree(itemDtos),
                        search,
                        sort,
                        new PagingInfo(pageSize, pageNumber, pageNumber > 1, hasNext)));
    }

    private void validatePaginationParams(int pageNumber, int pageSize) {
        if (pageNumber < 1) {
            throw new IncorrectParamException("Номер страницы должен быть не меньше 1");
        }
        if (pageSize <= 0) {
            throw new IncorrectParamException("Размер страницы должен быть положительным");
        }
        if (!ALLOWED_PAGE_SIZES.contains(pageSize)) {
            throw new IncorrectParamException(
                    "Недопустимый размер страницы: " + pageSize + ". Разрешены: " + ALLOWED_PAGE_SIZES);
        }
    }

    private Sort toSort(SortType sort) {
        return switch (sort) {
            case ALPHA -> Sort.by(Sort.Order.asc("title"), Sort.Order.asc("id"));
            case PRICE -> Sort.by(Sort.Order.asc("price"), Sort.Order.asc("id"));
            case NO -> Sort.by(Sort.Order.asc("id"));
        };
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<ItemDto> getItem(long id) {
        return itemRepository.findById(id)
                .switchIfEmpty(Mono.error(new ItemNotFoundException("Товар не найден")))
                .flatMap(this::mapFromCartIfExisted);
    }

    @Override
    @Transactional
    public Mono<ItemDto> updateCart(long itemId, ActionType action) {
        return cartItemRepository.findByItemId(itemId)
                .flatMap(cartItem -> itemRepository.findById(itemId)
                        .flatMap(item -> {
                            cartService.processCartItemAction(cartItem, action);
                            return persistCartItem(cartItem)
                                    .thenReturn(itemMapper.mapCartItemToItemDto(cartItem, item));
                        }))
                .switchIfEmpty(addItemToCartIfPossible(itemId, action));
    }

    private Mono<Void> persistCartItem(CartItem cartItem) {
        return cartItem.getCount() == 0
                ? cartItemRepository.delete(cartItem)
                : cartItemRepository.save(cartItem).then();
    }

    private Mono<ItemDto> addItemToCartIfPossible(long itemId, ActionType action) {
        return itemRepository.findById(itemId)
                .switchIfEmpty(Mono.error(new ItemNotFoundException("Товар не найден")))
                .flatMap(item -> {
                    if (ActionType.PLUS == action) {
                        CartItem newCartItem = new CartItem();
                        newCartItem.setItemId(item.getId());
                        newCartItem.setCount(1);
                        return cartItemRepository.save(newCartItem)
                                .map(saved -> itemMapper.mapCartItemToItemDto(saved, item));
                    }
                    return Mono.just(itemMapper.toDto(item));
                });
    }

    private Mono<ItemDto> mapFromCartIfExisted(Item item) {
        return cartItemRepository.findByItemId(item.getId())
                .map(cartItem -> itemMapper.mapCartItemToItemDto(cartItem, item))
                .switchIfEmpty(Mono.just(itemMapper.toDto(item)));
    }


    private List<List<ItemDto>> groupIntoRowsOfThree(List<ItemDto> items) {
        List<List<ItemDto>> result = new ArrayList<>();
        List<ItemDto> row = new ArrayList<>();

        for (ItemDto item : items) {
            row.add(item);
            if (row.size() == 3) {
                result.add(row);
                row = new ArrayList<>();
            }
        }

        if (!row.isEmpty()) {
            while (row.size() < 3) {
                row.add(new ItemDto(-1L, "", "", "", 0L, 0));
            }
            result.add(row);
        }

        return result;
    }
}


package ru.irrexp.practicum.mymarket.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    public ItemsPageResponse getItems(String search, SortType sort, int pageNumber, int pageSize) {
        validatePaginationParams(pageNumber, pageSize);

        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize, toSort(sort));

        Page<Item> page = isBlank(search)
                ? itemRepository.findAll(pageable)
                : itemRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                search, search, pageable);

        List<ItemDto> itemDtos = page.getContent().stream()
                .map(this::mapFromCartIfExisted)
                .toList();

        List<List<ItemDto>> grouped = groupIntoRowsOfThree(itemDtos);

        boolean hasPrevious = pageNumber > 1;
        boolean hasNext = page.hasNext();

        PagingInfo paging = new PagingInfo(pageSize, pageNumber, hasPrevious, hasNext);

        return new ItemsPageResponse(grouped, search, sort, paging);
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
    public ItemDto getItem(long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("Товар не найден"));
        return mapFromCartIfExisted(item);
    }

    @Override
    @Transactional
    public ItemDto updateCartFromItemPage(long itemId, ActionType action) {
        var cartItem = cartItemRepository.findByItemId(itemId);

        if (cartItem.isPresent()) {
            CartItem ci = cartItem.get();
            cartService.processCartItemAction(ci, action);
            return itemMapper.mapCartItemToItemDto(ci);
        } else {
            Item item = itemRepository.findById(itemId)
                    .orElseThrow(() -> new ItemNotFoundException("Товар не найден"));
            if (ActionType.PLUS == action) {
                CartItem newCartItem = new CartItem();
                newCartItem.setItem(item);
                newCartItem.setCount(1);
                cartItemRepository.save(newCartItem);
                return itemMapper.mapCartItemToItemDto(newCartItem);
            } else {
                return itemMapper.toDto(item);
            }
        }
    }

    @Override
    @Transactional
    public void updateCart(long id, String search, SortType sort, int pageNumber, int pageSize, ActionType action) {
        updateCartFromItemPage(id, action);
    }

    private ItemDto mapFromCartIfExisted(Item item) {
        var cartItem = cartItemRepository.findByItemId(item.getId());
        if (cartItem.isPresent()) {
            return itemMapper.mapCartItemToItemDto(cartItem.get());
        } else {
            return itemMapper.toDto(item);
        }
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


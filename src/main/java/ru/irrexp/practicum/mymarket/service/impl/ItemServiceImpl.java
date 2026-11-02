package ru.irrexp.practicum.mymarket.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.irrexp.practicum.mymarket.dto.ItemDto;
import ru.irrexp.practicum.mymarket.dto.ItemsPageResponse;
import ru.irrexp.practicum.mymarket.dto.PagingInfo;
import ru.irrexp.practicum.mymarket.entity.CartItem;
import ru.irrexp.practicum.mymarket.entity.Item;
import ru.irrexp.practicum.mymarket.enums.ActionType;
import ru.irrexp.practicum.mymarket.enums.SortType;
import ru.irrexp.practicum.mymarket.mapper.ItemMapper;
import ru.irrexp.practicum.mymarket.repository.CartItemRepository;
import ru.irrexp.practicum.mymarket.repository.ItemRepository;
import ru.irrexp.practicum.mymarket.service.ItemService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final CartItemRepository cartItemRepository;
    private final ItemMapper itemMapper;

    @Override
    @Transactional(readOnly = true)
    public ItemsPageResponse getItems(String search, SortType sort, int pageNumber, int pageSize) {
        List<Item> items = search == null ? itemRepository.findAll() : itemRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                search, search);

        items = applySorting(items, sort);

        int total = items.size();
        int fromIndex = (pageNumber - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);

        if (fromIndex >= total) {
            items = List.of();
        } else {
            items = items.subList(fromIndex, toIndex);
        }

        boolean hasPrevious = pageNumber > 1;
        boolean hasNext = toIndex < total;

        PagingInfo paging = new PagingInfo(pageSize, pageNumber, hasPrevious, hasNext);

        List<List<ItemDto>> grouped = groupIntoRowsOfThree(items);

        return new ItemsPageResponse(grouped, search, sort, paging);
    }

    @Override
    @Transactional(readOnly = true)
    public ItemDto getItem(long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Товар не найден"));
        return itemMapper.toDto(item);
    }

    @Override
    @Transactional
    public ItemDto updateCartFromItemPage(long id, ActionType action) {
        CartItem cartItem = cartItemRepository.findByItemId(id)
                .orElseGet(() -> {
                    CartItem newCartItem = new CartItem();
                    newCartItem.setItem(itemRepository.findById(id)
                                                .orElseThrow(() -> new IllegalArgumentException("Товар не найден")));
                    newCartItem.setCount(0);
                    return cartItemRepository.save(newCartItem);
                });

        int currentCount = cartItem.getCount();
        int newCount = switch (action) {
            case PLUS -> currentCount + 1;
            case MINUS -> Math.max(0, currentCount - 1);
            default -> currentCount;
        };

        cartItem.setCount(newCount);
        cartItemRepository.save(cartItem);

        Item item = cartItem.getItem();
        ItemDto dto = itemMapper.toDto(item);
        return new ItemDto(dto.id(), dto.title(), dto.description(), dto.imgPath(), dto.price(), newCount);
    }

    @Override
    @Transactional
    public void updateCart(long id, String search, SortType sort, int pageNumber, int pageSize, ActionType action) {
        updateCartFromItemPage(id, action);
    }

    private List<Item> applySorting(List<Item> items, SortType sort) {
        return switch (sort) {
            case ALPHA -> items.stream()
                    .sorted(Comparator.comparing(Item::getTitle, Comparator.nullsLast(Comparator.naturalOrder())))
                    .toList();
            case PRICE -> items.stream()
                    .sorted(Comparator.comparingLong(Item::getPrice))
                    .toList();
            default -> items;
        };
    }

    private List<List<ItemDto>> groupIntoRowsOfThree(List<Item> items) {
        List<List<ItemDto>> result = new ArrayList<>();
        List<ItemDto> row = new ArrayList<>();

        for (Item item : items) {
            row.add(itemMapper.toDto(item));
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


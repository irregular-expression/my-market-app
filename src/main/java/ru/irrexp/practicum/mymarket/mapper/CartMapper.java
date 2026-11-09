package ru.irrexp.practicum.mymarket.mapper;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.irrexp.practicum.mymarket.dto.CartResponse;
import ru.irrexp.practicum.mymarket.dto.ItemDto;
import ru.irrexp.practicum.mymarket.entity.CartItem;
import ru.irrexp.practicum.mymarket.entity.Item;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RequiredArgsConstructor
@Component
public class CartMapper {

    private final ItemMapper getItemMapper;

    public CartResponse toCartResponse(List<CartItem> cartItems, Map<Long, Item> itemMap) {
        return new CartResponse(
                toItemDtoList(cartItems, itemMap),
                totalSum(cartItems, itemMap)
        );
    }

    public List<ItemDto> toItemDtoList(List<CartItem> cartItems, Map<Long, Item> itemMap) {
        if (cartItems == null || cartItems.isEmpty()) {
            return Collections.emptyList();
        }
        return cartItems.stream()
                .map(ci -> {
                    Item item = itemMap.get(ci.getItemId());
                    return getItemMapper.mapCartItemToItemDto(ci, item);
                })
                .filter(Objects::nonNull)
                .toList();
    }

    public long totalSum(List<CartItem> cartItems, Map<Long, Item> itemMap) {
        if (cartItems == null || cartItems.isEmpty()) {
            return 0L;
        }
        return cartItems.stream()
                .mapToLong(ci -> {
                    Item item = itemMap.get(ci.getItemId());
                    if (item == null) {
                        return 0L;
                    }
                    return item.getPrice() * ci.getCount();
                })
                .sum();
    }
}


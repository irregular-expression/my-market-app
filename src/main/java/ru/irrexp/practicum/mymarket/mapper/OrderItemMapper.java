package ru.irrexp.practicum.mymarket.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.irrexp.practicum.mymarket.dto.ItemInOrderDto;
import ru.irrexp.practicum.mymarket.entity.CartItem;
import ru.irrexp.practicum.mymarket.entity.Item;
import ru.irrexp.practicum.mymarket.entity.OrderItem;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "orderId", ignore = true)
    @Mapping(target = "itemId", source = "item.id")
    OrderItem toOrderItem(CartItem cartItem, Item item);

    default List<ItemInOrderDto> mapOrderItemsToItemsInOrderDto(List<OrderItem> orderItems, Map<Long, Item> itemMap) {
        if (orderItems == null || orderItems.isEmpty()) {
            return Collections.emptyList();
        }
        return orderItems.stream()
                .map(oi -> {
                    Item item = itemMap.get(oi.getItemId());
                    if (item == null) {
                        return null;
                    }
                    return new ItemInOrderDto(
                            oi.getId(),
                            item.getTitle(),
                            oi.getPrice(),
                            oi.getCount()
                    );
                })
                .filter(Objects::nonNull)
                .toList();
    }
}
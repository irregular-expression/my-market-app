package ru.irrexp.practicum.mymarket.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.irrexp.practicum.mymarket.dto.ItemInOrderDto;
import ru.irrexp.practicum.mymarket.entity.CartItem;
import ru.irrexp.practicum.mymarket.entity.OrderItem;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "price", source = "item.price")
    OrderItem toOrderItem(CartItem cartItem);

    @Named("mapOrderItemsToItemsInOrderDto")
    default List<ItemInOrderDto> mapOrderItemsToItemsInOrderDto(List<OrderItem> orderItems) {
        return orderItems.stream()
                .map(oi -> new ItemInOrderDto(
                        oi.getId(),
                        oi.getItem().getTitle(),
                        oi.getPrice(),
                        oi.getCount()
                )).toList();
    }
}


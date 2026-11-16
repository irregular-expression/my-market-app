package ru.irrexp.practicum.mymarket.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.irrexp.practicum.mymarket.dto.OrderDto;
import ru.irrexp.practicum.mymarket.entity.Item;
import ru.irrexp.practicum.mymarket.entity.Order;
import ru.irrexp.practicum.mymarket.entity.OrderItem;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OrderMapper {

    private final OrderItemMapper itemMapper;

    public OrderDto toDtoWithItems(Order order, List<OrderItem> orderItems, Map<Long, Item> itemMap) {
        return new OrderDto(
                order.getId(),
                itemMapper.mapOrderItemsToItemsInOrderDto(orderItems, itemMap),
                order.getTotalSum()
        );
    }
}


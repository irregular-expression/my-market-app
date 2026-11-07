package ru.irrexp.practicum.mymarket.service;

import ru.irrexp.practicum.mymarket.dto.OrderDetailResponse;
import ru.irrexp.practicum.mymarket.dto.OrderDto;

import java.util.List;

public interface OrderService {

    List<OrderDto> getOrders();

    OrderDetailResponse getOrder(long id, boolean newOrder);

    long buy(); // возвращает id созданного заказа
}


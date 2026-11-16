package ru.irrexp.practicum.mymarket.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.irrexp.practicum.mymarket.dto.OrderDetailResponse;
import ru.irrexp.practicum.mymarket.dto.OrderDto;

public interface OrderService {

    Flux<OrderDto> getOrders();

    Mono<OrderDetailResponse> getOrder(long id, boolean newOrder);

    Mono<Long> buy(); // возвращает id созданного заказа
}


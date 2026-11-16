package ru.irrexp.practicum.mymarket.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;
import ru.irrexp.practicum.mymarket.controller.api.OrdersApi;
import ru.irrexp.practicum.mymarket.service.OrderService;

@Controller
@RequiredArgsConstructor
public class OrdersController implements OrdersApi {

    private final OrderService ordersService;

    @Override
    public Mono<Rendering> getOrders() {
        return ordersService.getOrders()
                .collectList()
                .map(list -> Rendering.view("orders")
                        .modelAttribute("orders", list)
                        .build());
    }

    @Override
    public Mono<Rendering> getOrder(long id, boolean newOrder) {
        return ordersService.getOrder(id, newOrder)
                .map(dto -> Rendering.view("order")
                        .modelAttribute("order", dto.order())
                        .modelAttribute("newOrder", dto.newOrder())
                        .build());
    }
}



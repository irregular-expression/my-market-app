package ru.irrexp.practicum.mymarket.controller.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;

@RequestMapping("/orders")
public interface OrdersApi {

    @GetMapping
    Mono<Rendering> getOrders();

    @GetMapping("/{id}")
    Mono<Rendering> getOrder(
            @PathVariable long id,
            @RequestParam(defaultValue = "false") boolean newOrder
    );

}

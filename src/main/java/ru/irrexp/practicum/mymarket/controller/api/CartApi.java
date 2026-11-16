package ru.irrexp.practicum.mymarket.controller.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;
import ru.irrexp.practicum.mymarket.dto.UpdateCartRequest;
import ru.irrexp.practicum.mymarket.enums.ActionType;


@RequestMapping("/cart")
public interface CartApi {

    @GetMapping("/items")
    Mono<Rendering> getCartItems();

    @PostMapping("/items")
    Mono<Rendering> updateCartItem(
            @ModelAttribute UpdateCartRequest updateCartRequest
            );
}

package ru.irrexp.practicum.mymarket.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;
import ru.irrexp.practicum.mymarket.controller.api.CartApi;
import ru.irrexp.practicum.mymarket.dto.UpdateCartRequest;
import ru.irrexp.practicum.mymarket.enums.ActionType;
import ru.irrexp.practicum.mymarket.service.CartService;

@Controller
@RequiredArgsConstructor
public class CartController implements CartApi {

    private final CartService cartService;

    @Override
    public Mono<Rendering> getCartItems() {
        return cartService.getCart()
                .map(dto -> Rendering.view("cart")
                        .modelAttribute("items", dto.items())
                        .modelAttribute("total", dto.total())
                        .build());
    }

    @Override
    public Mono<Rendering> updateCartItem(UpdateCartRequest updateCartRequest) {
        return cartService.updateCartItem(updateCartRequest.id(), updateCartRequest.action())
                .then(Mono.just(Rendering.redirectTo("/cart/items").build()));
    }
}



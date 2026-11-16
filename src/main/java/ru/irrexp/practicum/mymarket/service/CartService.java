package ru.irrexp.practicum.mymarket.service;

import reactor.core.publisher.Mono;
import ru.irrexp.practicum.mymarket.dto.CartResponse;
import ru.irrexp.practicum.mymarket.entity.CartItem;
import ru.irrexp.practicum.mymarket.enums.ActionType;

public interface CartService {

    Mono<CartResponse> getCart();

    Mono<CartResponse> updateCartItem(long itemId, ActionType action);

    void processCartItemAction(CartItem cartItem, ActionType action);
}


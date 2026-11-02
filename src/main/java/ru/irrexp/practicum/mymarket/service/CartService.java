package ru.irrexp.practicum.mymarket.service;

import ru.irrexp.practicum.mymarket.dto.CartResponse;
import ru.irrexp.practicum.mymarket.enums.ActionType;

public interface CartService {

    CartResponse getCart();

    CartResponse updateCartItem(long id, ActionType action);
}


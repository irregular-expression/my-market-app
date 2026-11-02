package ru.irrexp.practicum.mymarket.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.irrexp.practicum.mymarket.dto.CartResponse;
import ru.irrexp.practicum.mymarket.entity.CartItem;
import ru.irrexp.practicum.mymarket.enums.ActionType;
import ru.irrexp.practicum.mymarket.mapper.CartMapper;
import ru.irrexp.practicum.mymarket.repository.CartItemRepository;
import ru.irrexp.practicum.mymarket.service.CartService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartItemRepository cartItemRepository;
    private final CartMapper cartMapper;

    @Override
    @Transactional(readOnly = true)
    public CartResponse getCart() {
        List<CartItem> cartItems = cartItemRepository.findAll();
        return cartMapper.toCartResponse(cartItems);
    }

    @Override
    @Transactional
    public CartResponse updateCartItem(long id, ActionType action) {
        CartItem cartItem = cartItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Товар не найден в корзине"));

        int currentCount = cartItem.getCount();
        int newCount = switch (action) {
            case PLUS -> currentCount + 1;
            case MINUS -> Math.max(0, currentCount - 1);
            case DELETE -> 0;
        };

        if (newCount == 0) {
            cartItemRepository.delete(cartItem);
        } else {
            cartItem.setCount(newCount);
            cartItemRepository.save(cartItem);
        }

        List<CartItem> updatedItems = cartItemRepository.findAll();
        return cartMapper.toCartResponse(updatedItems);
    }
}


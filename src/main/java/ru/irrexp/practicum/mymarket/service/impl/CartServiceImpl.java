package ru.irrexp.practicum.mymarket.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import ru.irrexp.practicum.mymarket.dto.CartResponse;
import ru.irrexp.practicum.mymarket.entity.CartItem;
import ru.irrexp.practicum.mymarket.entity.Item;
import ru.irrexp.practicum.mymarket.enums.ActionType;
import ru.irrexp.practicum.mymarket.exception.ItemNotFoundException;
import ru.irrexp.practicum.mymarket.mapper.CartMapper;
import ru.irrexp.practicum.mymarket.repository.CartItemRepository;
import ru.irrexp.practicum.mymarket.repository.ItemRepository;
import ru.irrexp.practicum.mymarket.service.CartService;

import java.util.List;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartItemRepository cartItemRepository;
    private final ItemRepository itemRepository;
    private final CartMapper cartMapper;

    @Override
    @Transactional(readOnly = true)
    public Mono<CartResponse> getCart() {
        return cartItemRepository.findAll()
                .collectList()
                .flatMap(cartItems -> {
                    if (cartItems.isEmpty()) {
                        return Mono.just(new CartResponse(List.of(), 0L));
                    }
                    List<Long> itemIds = cartItems.stream()
                            .map(CartItem::getItemId)
                            .toList();
                    return itemRepository.findAllById(itemIds)
                            .collectMap(Item::getId, Function.identity())
                            .map(itemMap -> cartMapper.toCartResponse(cartItems, itemMap));
                });
    }

    @Override
    @Transactional
    public Mono<CartResponse> updateCartItem(long itemId, ActionType action) {
        return cartItemRepository.findByItemId(itemId)
                .switchIfEmpty(Mono.error(new ItemNotFoundException("Товар не найден в корзине")))
                .flatMap(cartItem -> {
                    processCartItemAction(cartItem, action);
                    Mono<List<CartItem>> nextState;
                    if (cartItem.getCount() == 0) {
                        nextState = cartItemRepository.delete(cartItem)
                                .then(cartItemRepository.findAll().collectList());
                    } else {
                        nextState = cartItemRepository.save(cartItem)
                                .then(cartItemRepository.findAll().collectList());
                    }
                    return nextState.flatMap(items -> {
                        if (items.isEmpty()) {
                            return Mono.just(new CartResponse(List.of(), 0L));
                        }
                        List<Long> itemIds = items.stream()
                                .map(CartItem::getItemId)
                                .toList();
                        return itemRepository.findAllById(itemIds)
                                .collectMap(Item::getId, Function.identity())
                                .map(itemMap -> cartMapper.toCartResponse(items, itemMap));
                    });
                });
    }

    @Override
    public void processCartItemAction(CartItem cartItem, ActionType action) {
        int currentCount = cartItem.getCount();
        int newCount = switch (action) {
            case PLUS -> currentCount + 1;
            case MINUS -> Math.max(0, currentCount - 1);
            case DELETE -> 0;
        };
        cartItem.setCount(newCount);
    }
}
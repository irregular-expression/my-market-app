package ru.irrexp.practicum.mymarket.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.irrexp.practicum.mymarket.dto.CartResponse;
import ru.irrexp.practicum.mymarket.entity.CartItem;
import ru.irrexp.practicum.mymarket.entity.Item;
import ru.irrexp.practicum.mymarket.enums.ActionType;
import ru.irrexp.practicum.mymarket.exception.ItemNotFoundException;
import ru.irrexp.practicum.mymarket.repository.CartItemRepository;
import ru.irrexp.practicum.mymarket.repository.ItemRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class CartServiceTest {

    @Autowired
    private CartService cartService;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ItemRepository itemRepository;

    @BeforeEach
    void setUp() {
        cartItemRepository.deleteAll();
        itemRepository.deleteAll();
    }

    @Test
    void getCart_returnsEmptyResponseWhenNoItems() {
        CartResponse response = cartService.getCart();
        assertThat(response.items()).isEmpty();
        assertThat(response.total()).isZero();
    }

    @Test
    void getCart_returnsItemsFromRepository() {
        Item item = new Item();
        item.setTitle("Test Item");
        item.setPrice(100L);
        item = itemRepository.save(item);

        CartItem cartItem = new CartItem();
        cartItem.setItem(item);
        cartItem.setCount(2);
        cartItemRepository.save(cartItem);

        CartResponse response = cartService.getCart();
        assertThat(response.items()).hasSize(1);
        assertThat(response.total()).isEqualTo(200L);
    }

    @Test
    void updateCartItem_plusIncreasesCount() {
        Item item = new Item();
        item.setTitle("Item A");
        item.setPrice(50L);
        item = itemRepository.save(item);

        CartItem cartItem = new CartItem();
        cartItem.setItem(item);
        cartItem.setCount(1);
        cartItemRepository.save(cartItem);

        CartResponse response = cartService.updateCartItem(item.getId(), ActionType.PLUS);
        assertThat(response.items()).hasSize(1);
        assertThat(response.items().getFirst().count()).isEqualTo(2);
        assertThat(response.total()).isEqualTo(100L);
    }

    @Test
    void updateCartItem_minusDecreasesCountAndRemovesWhenZero() {
        Item item = new Item();
        item.setTitle("Item B");
        item.setPrice(30L);
        item = itemRepository.save(item);

        CartItem cartItem = new CartItem();
        cartItem.setItem(item);
        cartItem.setCount(2);
        cartItemRepository.saveAndFlush(cartItem);

        CartResponse response = cartService.updateCartItem(item.getId(), ActionType.MINUS);
        assertThat(response.items().getFirst().count()).isEqualTo(1);
        assertThat(response.total()).isEqualTo(30L);

        response = cartService.updateCartItem(item.getId(), ActionType.MINUS);
        assertThat(response.items()).isEmpty();
        assertThat(response.total()).isZero();
        assertThat(cartItemRepository.findByItemId(item.getId())).isEmpty();
    }

    @Test
    void updateCartItem_deleteRemovesItem() {
        Item item = new Item();
        item.setTitle("Item C");
        item.setPrice(70L);
        item = itemRepository.save(item);

        CartItem cartItem = new CartItem();
        cartItem.setItem(item);
        cartItem.setCount(3);
        cartItemRepository.save(cartItem);

        CartResponse response = cartService.updateCartItem(item.getId(), ActionType.DELETE);
        assertThat(response.items()).isEmpty();
        assertThat(response.total()).isZero();
        assertThat(cartItemRepository.findByItemId(item.getId())).isEmpty();
    }

    @Test
    void updateCartItem_throwsWhenItemNotInCart() {
        Item item = new Item();
        item.setTitle("Nonexistent in cart");
        item.setPrice(99L);
        item = itemRepository.save(item);

        final long id = item.getId();
        assertThatThrownBy(() -> cartService.updateCartItem(id, ActionType.PLUS))
                .isInstanceOf(ItemNotFoundException.class)
                .hasMessageContaining("Товар не найден в корзине");
    }
}


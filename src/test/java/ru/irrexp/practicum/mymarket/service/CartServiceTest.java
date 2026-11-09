package ru.irrexp.practicum.mymarket.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;
import ru.irrexp.practicum.mymarket.dto.ItemDto;
import ru.irrexp.practicum.mymarket.entity.CartItem;
import ru.irrexp.practicum.mymarket.entity.Item;
import ru.irrexp.practicum.mymarket.enums.ActionType;
import ru.irrexp.practicum.mymarket.exception.ItemNotFoundException;
import ru.irrexp.practicum.mymarket.repository.CartItemRepository;
import ru.irrexp.practicum.mymarket.repository.ItemRepository;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class CartServiceTest {

    @Autowired
    private CartService cartService;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @BeforeEach
    void cleanUp() {
        cartItemRepository.deleteAll().then(itemRepository.deleteAll()).block();
    }

    private Item item(String title, long price) {
        Item item = new Item();
        item.setTitle(title);
        item.setDescription("Описание " + title);
        item.setImgPath("/img/" + title + ".png");
        item.setPrice(price);
        return item;
    }

    private CartItem cartItem(long itemId, int count) {
        CartItem cartItem = new CartItem();
        cartItem.setItemId(itemId);
        cartItem.setCount(count);
        return cartItem;
    }

    @Test
    @DisplayName("getCart — пустая корзина: пустой список и нулевая сумма")
    void getCart_empty() {
        StepVerifier.create(cartService.getCart())
                .assertNext(response -> {
                    assertThat(response.items()).isEmpty();
                    assertThat(response.total()).isZero();
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("getCart — два товара: DTO содержат счётчики корзины, сумма корректна")
    void getCart_withItems() {
        Item milk = itemRepository.save(item("Молоко", 89L)).block();
        Item bread = itemRepository.save(item("Хлеб", 45L)).block();
        cartItemRepository.save(cartItem(milk.getId(), 2)).block();
        cartItemRepository.save(cartItem(bread.getId(), 1)).block();

        StepVerifier.create(cartService.getCart())
                .assertNext(response -> {
                    assertThat(response.items()).hasSize(2);
                    assertThat(response.total()).isEqualTo(223L);

                    ItemDto milkDto = response.items().stream()
                            .filter(dto -> dto.id() == milk.getId())
                            .findFirst()
                            .orElseThrow();
                    assertThat(milkDto.title()).isEqualTo("Молоко");
                    assertThat(milkDto.price()).isEqualTo(89L);
                    assertThat(milkDto.count()).isEqualTo(2);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("updateCartItem PLUS — счётчик увеличивается, ответ отражает новую корзину")
    void updateCartItem_plus_increasesCount() {
        Item milk = itemRepository.save(item("Молоко", 89L)).block();
        cartItemRepository.save(cartItem(milk.getId(), 1)).block();

        StepVerifier.create(cartService.updateCartItem(milk.getId(), ActionType.PLUS))
                .assertNext(response -> {
                    assertThat(response.items()).hasSize(1);
                    assertThat(response.items().get(0).count()).isEqualTo(2);
                    assertThat(response.total()).isEqualTo(178L);
                })
                .verifyComplete();

        assertThat(cartItemRepository.findByItemId(milk.getId()).block().getCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("updateCartItem MINUS до нуля — позиция удаляется, корзина пустеет")
    void updateCartItem_minusToZero_removesPosition() {
        Item milk = itemRepository.save(item("Молоко", 89L)).block();
        cartItemRepository.save(cartItem(milk.getId(), 1)).block();

        StepVerifier.create(cartService.updateCartItem(milk.getId(), ActionType.MINUS))
                .assertNext(response -> {
                    assertThat(response.items()).isEmpty();
                    assertThat(response.total()).isZero();
                })
                .verifyComplete();

        assertThat(cartItemRepository.findByItemId(milk.getId()).block()).isNull();
    }

    @Test
    @DisplayName("updateCartItem DELETE — позиция удаляется независимо от счётчика")
    void updateCartItem_delete_removesPosition() {
        Item milk = itemRepository.save(item("Молоко", 89L)).block();
        cartItemRepository.save(cartItem(milk.getId(), 5)).block();

        StepVerifier.create(cartService.updateCartItem(milk.getId(), ActionType.DELETE))
                .assertNext(response -> assertThat(response.items()).isEmpty())
                .verifyComplete();

        assertThat(cartItemRepository.findByItemId(milk.getId()).block()).isNull();
    }

    @Test
    @DisplayName("updateCartItem — товара нет в корзине: ItemNotFoundException")
    void updateCartItem_missingCartEntry_throws() {
        Item milk = itemRepository.save(item("Молоко", 89L)).block();

        StepVerifier.create(cartService.updateCartItem(milk.getId(), ActionType.PLUS))
                .expectError(ItemNotFoundException.class)
                .verify();
    }

    @Test
    @DisplayName("processCartItemAction — граничные значения счётчика")
    void processCartItemAction_boundaries() {
        CartItem cartItem = new CartItem();
        cartItem.setCount(0);

        cartService.processCartItemAction(cartItem, ActionType.MINUS);
        assertThat(cartItem.getCount()).isZero();

        cartService.processCartItemAction(cartItem, ActionType.PLUS);
        assertThat(cartItem.getCount()).isEqualTo(1);

        cartService.processCartItemAction(cartItem, ActionType.DELETE);
        assertThat(cartItem.getCount()).isZero();
    }
}


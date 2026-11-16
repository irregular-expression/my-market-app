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
import ru.irrexp.practicum.mymarket.enums.SortType;
import ru.irrexp.practicum.mymarket.exception.IncorrectParamException;
import ru.irrexp.practicum.mymarket.exception.ItemNotFoundException;
import ru.irrexp.practicum.mymarket.repository.CartItemRepository;
import ru.irrexp.practicum.mymarket.repository.ItemRepository;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class ItemServiceTest {

    @Autowired
    private ItemService itemService;

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
        cartItem.setCount(count);
        cartItem.setItemId(itemId);
        return cartItem;
    }

    private List<Long> idsInRow(List<List<ItemDto>> grouped, int rowIndex) {
        return grouped.get(rowIndex).stream().map(ItemDto::id).toList();
    }

    @Test
    @DisplayName("getItems — пагинация: первая страница, hasNext=true")
    void getItems_paginationFirstPage() {
        List<Item> saved = itemRepository.saveAll(
                        IntStream.rangeClosed(1, 7).mapToObj(i -> item("Товар " + i, i * 10L)).toList())
                .collectList().block();

        StepVerifier.create(itemService.getItems(null, SortType.NO, 1, 2))
                .assertNext(response -> {
                    assertThat(idsInRow(response.items(), 0)).isEqualTo(
                            List.of(saved.get(0).getId(), saved.get(1).getId(), -1L));
                    assertThat(response.paging().pageNumber()).isEqualTo(1);
                    assertThat(response.paging().hasNext()).isTrue();
                    assertThat(response.paging().hasPrevious()).isFalse();
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("getItems — пагинация: последняя неполная страница, hasNext=false, без пустышек сверх остатка")
    void getItems_paginationLastPage() {
        List<Item> saved = itemRepository.saveAll(
                        IntStream.rangeClosed(1, 7).mapToObj(i -> item("Товар " + i, i * 10L)).toList())
                .collectList().block();

        StepVerifier.create(itemService.getItems(null, SortType.NO, 4, 2))
                .assertNext(response -> {
                    assertThat(response.items()).hasSize(1);
                    assertThat(response.items().get(0).get(0).id()).isEqualTo(saved.get(6).getId());
                    assertThat(response.paging().hasNext()).isFalse();
                    assertThat(response.paging().hasPrevious()).isTrue();
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("getItems — пустая страница за пределами каталога")
    void getItems_pageBeyondCatalog_isEmpty() {
        itemRepository.save(item("Молоко", 89L)).block();

        StepVerifier.create(itemService.getItems(null, SortType.NO, 5, 5))
                .assertNext(response -> {
                    assertThat(response.items()).isEmpty();
                    assertThat(response.paging().hasNext()).isFalse();
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("getItems — сортировка по цене возрастает, при равенстве по id")
    void getItems_sortByPrice() {
        itemRepository.saveAll(List.of(
                        item("Яблоко", 50L),
                        item("Апельсин", 10L),
                        item("Банан", 50L)))
                .collectList().block();

        StepVerifier.create(itemService.getItems(null, SortType.PRICE, 1, 5))
                .assertNext(response -> {
                    List<String> titles = response.items().stream()
                            .flatMap(List::stream)
                            .map(ItemDto::title)
                            .filter(t -> !t.isEmpty())
                            .toList();
                    assertThat(titles).containsExactly("Апельсин", "Яблоко", "Банан");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("getItems — поиск без учёта регистра по title и description")
    void getItems_search() {
        itemRepository.saveAll(List.of(
                        item("Moloko", 89L),
                        item("Bread", 45L),
                        item("Kefir", 60L)))
                .collectList().block();
        Item byDescription = item("Cheese", 100L);
        byDescription.setDescription(" Свежий MOLOKO продукт ");
        itemRepository.save(byDescription).block();

        StepVerifier.create(itemService.getItems("moloko", SortType.NO, 1, 10))
                .assertNext(response -> {
                    List<String> titles = response.items().stream()
                            .flatMap(List::stream)
                            .map(ItemDto::title)
                            .filter(t -> !t.isEmpty())
                            .toList();
                    assertThat(titles).containsExactlyInAnyOrder("Moloko", "Cheese");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("getItems — pageNumber меньше 1: IncorrectParamException")
    void getItems_invalidPageNumber() {
        StepVerifier.create(itemService.getItems(null, SortType.NO, 0, 5))
                .expectError(IncorrectParamException.class)
                .verify();
    }

    @Test
    @DisplayName("getItems — недопустимый размер страницы: IncorrectParamException")
    void getItems_invalidPageSize() {
        StepVerifier.create(itemService.getItems(null, SortType.NO, 1, 3))
                .expectError(IncorrectParamException.class)
                .verify();
    }

    @Test
    @DisplayName("getItem — существующий товар вне корзины: count равен нулю")
    void getItem_notInCart() {
        Item saved = itemRepository.save(item("Молоко", 89L)).block();

        StepVerifier.create(itemService.getItem(saved.getId()))
                .assertNext(dto -> {
                    assertThat(dto.title()).isEqualTo("Молоко");
                    assertThat(dto.count()).isZero();
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("getItem — несуществующий товар: ItemNotFoundException")
    void getItem_missing_throws() {
        StepVerifier.create(itemService.getItem(4242L))
                .expectError(ItemNotFoundException.class)
                .verify();
    }

    @Test
    @DisplayName("getItem — товар в корзине: count берётся из корзины")
    void getItem_inCart() {
        Item saved = itemRepository.save(item("Молоко", 89L)).block();
        cartItemRepository.save(cartItem(saved.getId(), 3)).block();

        StepVerifier.create(itemService.getItem(saved.getId()))
                .assertNext(dto -> assertThat(dto.count()).isEqualTo(3))
                .verifyComplete();
    }


    @Test
    @DisplayName("updateCart PLUS — товар без позиции в корзине добавляется со счётчиком 1")
    void updateCart_plusCreatesCartEntry() {
        Item saved = itemRepository.save(item("Молоко", 89L)).block();

        StepVerifier.create(itemService.updateCart(saved.getId(), ActionType.PLUS))
                .assertNext(dto -> assertThat(dto.count()).isEqualTo(1))
                .verifyComplete();

        assertThat(cartItemRepository.findByItemId(saved.getId()).block().getCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("updateCart PLUS — существующая позиция увеличивается")
    void updateCart_plusIncrements() {
        Item saved = itemRepository.save(item("Молоко", 89L)).block();
        CartItem ci = new CartItem();
        ci.setItemId(saved.getId());
        ci.setCount(2);
        cartItemRepository.save(ci).block();

        StepVerifier.create(itemService.updateCart(saved.getId(), ActionType.PLUS))
                .assertNext(dto -> assertThat(dto.count()).isEqualTo(3))
                .verifyComplete();
    }

    @Test
    @DisplayName("updateCart MINUS до нуля — позиция удаляется из корзины")
    void updateCart_minusToZero_removesEntry() {
        Item saved = itemRepository.save(item("Молоко", 89L)).block();
        CartItem ci = new CartItem();
        ci.setItemId(saved.getId());
        ci.setCount(1);
        cartItemRepository.save(ci).block();

        StepVerifier.create(itemService.updateCart(saved.getId(), ActionType.MINUS))
                .assertNext(dto -> assertThat(dto.count()).isZero())
                .verifyComplete();

        assertThat(cartItemRepository.findByItemId(saved.getId()).block()).isNull();
    }

    @Test
    @DisplayName("updateCart MINUS — товара нет в корзине: DTO без изменений, позиция не создаётся")
    void updateCart_minusWithoutCartEntry_noOp() {
        Item saved = itemRepository.save(item("Молоко", 89L)).block();

        StepVerifier.create(itemService.updateCart(saved.getId(), ActionType.MINUS))
                .assertNext(dto -> assertThat(dto.count()).isZero())
                .verifyComplete();

        assertThat(cartItemRepository.findByItemId(saved.getId()).block()).isNull();
    }

    @Test
    @DisplayName("updateCart — несуществующий товар: ItemNotFoundException")
    void updateCart_missingItem_throws() {
        StepVerifier.create(itemService.updateCart(4242L, ActionType.PLUS))
                .expectError(ItemNotFoundException.class)
                .verify();
    }
}


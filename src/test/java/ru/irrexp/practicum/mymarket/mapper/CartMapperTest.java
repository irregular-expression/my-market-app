package ru.irrexp.practicum.mymarket.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.irrexp.practicum.mymarket.entity.CartItem;
import ru.irrexp.practicum.mymarket.entity.Item;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CartMapperTest {

    private final ItemMapper itemMapper = new ItemMapperImpl();
    private final CartMapper mapper = new CartMapper(itemMapper);

    private Item item(long id, String title, long price) {
        Item item = new Item();
        item.setId(id);
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
    @DisplayName("toCartResponse — пустая и null-корзина: пустой список и нулевая сумма")
    void toCartResponse_nullOrEmpty() {
        assertThat(mapper.toCartResponse(null, Map.of()).items()).isEmpty();
        assertThat(mapper.toCartResponse(null, Map.of()).total()).isZero();
        assertThat(mapper.toCartResponse(List.of(), Map.of()).items()).isEmpty();
        assertThat(mapper.toCartResponse(List.of(), Map.of()).total()).isZero();
    }

    @Test
    @DisplayName("toCartResponse — товары и сумма считаются по ценам каталога и счётчикам корзины")
    void toCartResponse_mapsItemsAndSumsTotal() {
        Map<Long, Item> itemMap = Map.of(
                1L, item(1L, "Молоко", 89L),
                2L, item(2L, "Хлеб", 45L));
        List<CartItem> cartItems = List.of(cartItem(1L, 2), cartItem(2L, 1));

        var response = mapper.toCartResponse(cartItems, itemMap);

        assertThat(response.items()).hasSize(2);
        assertThat(response.total()).isEqualTo(223L);

        var milk = response.items().stream()
                .filter(dto -> dto.id() == 1L)
                .findFirst()
                .orElseThrow();
        assertThat(milk.title()).isEqualTo("Молоко");
        assertThat(milk.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("toItemDtoList — null и пустой список дают пустой результат")
    void toItemDtoList_nullOrEmpty() {
        assertThat(mapper.toItemDtoList(null, Map.of())).isEmpty();
        assertThat(mapper.toItemDtoList(List.of(), Map.of())).isEmpty();
    }

    @Test
    @DisplayName("toItemDtoList — позиция без товара в каталоге: DTO с неполными данными")
    void toItemDtoList_missingItem_partialDto() {
        Map<Long, Item> itemMap = Map.of(1L, item(1L, "Молоко", 89L));
        List<CartItem> cartItems = List.of(cartItem(1L, 1), cartItem(99L, 4));

        var dtos = mapper.toItemDtoList(cartItems, itemMap);

        assertThat(dtos).hasSize(2);
        var broken = dtos.stream()
                .filter(dto -> dto.count() == 4)
                .findFirst()
                .orElseThrow();
        assertEquals(0, broken.id());
        assertThat(broken.title()).isNull();
    }

    @Test
    @DisplayName("totalSum — позиция без товара в каталоге вносит ноль в сумму")
    void totalSum_missingItem_countsAsZero() {
        Map<Long, Item> itemMap = Map.of(1L, item(1L, "Молоко", 89L));
        List<CartItem> cartItems = List.of(cartItem(1L, 2), cartItem(99L, 10));

        assertThat(mapper.totalSum(cartItems, itemMap)).isEqualTo(178L);
    }

    @Test
    @DisplayName("totalSum — null и пустой список дают ноль")
    void totalSum_nullOrEmpty() {
        assertThat(mapper.totalSum(null, Map.of())).isZero();
        assertThat(mapper.totalSum(List.of(), Map.of())).isZero();
    }
}


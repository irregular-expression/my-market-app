package ru.irrexp.practicum.mymarket.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.irrexp.practicum.mymarket.dto.ItemDto;
import ru.irrexp.practicum.mymarket.entity.CartItem;
import ru.irrexp.practicum.mymarket.entity.Item;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ItemMapperTest {

    private final ItemMapper mapper = new ItemMapperImpl();

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
    @DisplayName("toDto — копирует поля товара, count игнорируется")
    void toDto_copiesFields_ignoresCount() {
        Item source = item("Молоко", 89L);
        source.setId(1L);

        ItemDto dto = mapper.toDto(source);

        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.title()).isEqualTo("Молоко");
        assertThat(dto.description()).isEqualTo("Описание Молоко");
        assertThat(dto.imgPath()).isEqualTo("/img/Молоко.png");
        assertThat(dto.price()).isEqualTo(89L);
        assertThat(dto.count()).isZero();
    }

    @Test
    @DisplayName("toDto — null на входе даёт null на выходе")
    void toDto_nullSource_returnsNull() {
        assertThat(mapper.toDto(null)).isNull();
    }

    @Test
    @DisplayName("mapCartItemToItemDto — поля из товара, count из позиции корзины")
    void mapCartItemToItemDto_mapsFromBothSources() {
        Item source = item("Хлеб", 45L);
        source.setId(2L);
        CartItem cartItem = cartItem(2L, 3);

        ItemDto dto = mapper.mapCartItemToItemDto(cartItem, source);

        assertThat(dto.id()).isEqualTo(2L);
        assertThat(dto.title()).isEqualTo("Хлеб");
        assertThat(dto.description()).isEqualTo("Описание Хлеб");
        assertThat(dto.imgPath()).isEqualTo("/img/Хлеб.png");
        assertThat(dto.price()).isEqualTo(45L);
        assertThat(dto.count()).isEqualTo(3);
    }

    @Test
    @DisplayName("mapCartItemToItemDto — оба источника null: возвращается null")
    void mapCartItemToItemDto_bothNull_returnsNull() {
        assertThat(mapper.mapCartItemToItemDto(null, null)).isNull();
    }

    @Test
    @DisplayName("mapCartItemToItemDto — item null: заполняется только count")
    void mapCartItemToItemDto_itemNull_keepsOnlyCount() {
        CartItem cartItem = cartItem(42L, 2);

        ItemDto dto = mapper.mapCartItemToItemDto(cartItem, null);

        assertThat(dto.count()).isEqualTo(2);
        assertEquals(0, dto.id());
        assertThat(dto.title()).isNull();
        assertEquals(0, dto.price());
    }
}


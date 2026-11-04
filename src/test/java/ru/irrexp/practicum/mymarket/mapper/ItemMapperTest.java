package ru.irrexp.practicum.mymarket.mapper;

import org.junit.jupiter.api.Test;
import ru.irrexp.practicum.mymarket.dto.ItemDto;
import ru.irrexp.practicum.mymarket.entity.CartItem;
import ru.irrexp.practicum.mymarket.entity.Item;

import static org.assertj.core.api.Assertions.assertThat;

class ItemMapperTest {

    private final ItemMapper itemMapper = new ItemMapperImpl();

    @Test
    void toDto_mapsAllFieldsCorrectly() {
        Item item = new Item();
        item.setId(5L);
        item.setTitle("Full Item");
        item.setDescription("Detailed description");
        item.setImgPath("/static/full.png");
        item.setPrice(999L);

        ItemDto dto = itemMapper.toDto(item);

        assertThat(dto.id()).isEqualTo(5L);
        assertThat(dto.title()).isEqualTo("Full Item");
        assertThat(dto.description()).isEqualTo("Detailed description");
        assertThat(dto.imgPath()).isEqualTo("/static/full.png");
        assertThat(dto.price()).isEqualTo(999L);
    }

    @Test
    void mapCartItemToItemDto_handlesNullAndExtractsItem() {
        assertThat(itemMapper.mapCartItemToItemDto(null)).isNull();

        Item item = new Item();
        item.setId(7L);
        item.setTitle("From Cart");
        item.setPrice(300L);

        CartItem cartItem = new CartItem();
        cartItem.setId(1L);
        cartItem.setItem(item);
        cartItem.setCount(2);

        ItemDto dto = itemMapper.mapCartItemToItemDto(cartItem);

        assertThat(dto.id()).isEqualTo(7L);
        assertThat(dto.title()).isEqualTo("From Cart");
        assertThat(dto.price()).isEqualTo(300L);
        assertThat(dto.count()).isEqualTo(2);
    }
}


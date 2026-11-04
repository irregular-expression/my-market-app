package ru.irrexp.practicum.mymarket.mapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.irrexp.practicum.mymarket.dto.CartResponse;
import ru.irrexp.practicum.mymarket.dto.ItemDto;
import ru.irrexp.practicum.mymarket.entity.CartItem;
import ru.irrexp.practicum.mymarket.entity.Item;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CartMapperImpl.class, ItemMapperImpl.class})
class CartMapperTest {

    @Autowired
    private CartMapper cartMapper;

    @Test
    void toCartResponse_mapsItemsAndCalculatesTotal() {
        Item item = new Item();
        item.setId(1L);
        item.setTitle("Test Item");
        item.setDescription("Desc");
        item.setImgPath("/static/test.png");
        item.setPrice(100L);

        CartItem cartItem = new CartItem();
        cartItem.setItem(item);
        cartItem.setCount(3);

        List<CartItem> cartItems = List.of(cartItem);
        CartResponse response = cartMapper.toCartResponse(cartItems);

        assertThat(response.items()).hasSize(1);
        ItemDto dto = response.items().getFirst();
        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.title()).isEqualTo("Test Item");
        assertThat(dto.price()).isEqualTo(100L);
        assertThat(dto.count()).isEqualTo(3);
        assertThat(response.total()).isEqualTo(300L);
    }

    @Test
    void toCartResponse_handlesNullList() {
        CartResponse response = cartMapper.toCartResponse(null);
        assertThat(response.items()).isEmpty();
        assertThat(response.total()).isZero();
    }

    @Test
    void toCartResponse_calculatesTotalFromMultipleItems() {
        Item item1 = new Item();
        item1.setId(2L);
        item1.setTitle("Item A");
        item1.setPrice(200L);

        Item item2 = new Item();
        item2.setId(3L);
        item2.setTitle("Item B");
        item2.setPrice(50L);

        CartItem ci1 = new CartItem();
        ci1.setId(1L);
        ci1.setItem(item1);
        ci1.setCount(2);

        CartItem ci2 = new CartItem();
        ci2.setId(2L);
        ci2.setItem(item2);
        ci2.setCount(4);

        List<CartItem> cartItems = List.of(ci1, ci2);
        CartResponse response = cartMapper.toCartResponse(cartItems);

        assertThat(response.total()).isEqualTo(600L);
    }
}


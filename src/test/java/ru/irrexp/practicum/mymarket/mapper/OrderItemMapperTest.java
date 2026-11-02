package ru.irrexp.practicum.mymarket.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import ru.irrexp.practicum.mymarket.entity.CartItem;
import ru.irrexp.practicum.mymarket.entity.Item;
import ru.irrexp.practicum.mymarket.entity.OrderItem;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class OrderItemMapperTest {

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Test
    void toOrderItem_mapsPriceFromItemAndIgnoresIdAndOrder() {
        Item item = new Item();
        item.setId(10L);
        item.setTitle("Order Item Test");
        item.setPrice(800L);

        CartItem cartItem = new CartItem();
        cartItem.setItem(item);
        cartItem.setCount(3);

        OrderItem orderItem = orderItemMapper.toOrderItem(cartItem);

        assertThat(orderItem.getPrice()).isEqualTo(800L);
        assertThat(orderItem.getCount()).isEqualTo(3);
        assertThat(orderItem.getId()).isNull();
        assertThat(orderItem.getOrder()).isNull();
    }

    @Test
    void toOrderItem_preservesOtherCartItemFieldsNotExplicitlyMapped() {
        Item item = new Item();
        item.setId(11L);
        item.setTitle("Another Test");
        item.setPrice(550L);

        CartItem cartItem = new CartItem();
        cartItem.setItem(item);
        cartItem.setCount(1);

        OrderItem orderItem = orderItemMapper.toOrderItem(cartItem);

        assertThat(orderItem.getPrice()).isEqualTo(550L);
        assertThat(orderItem.getCount()).isEqualTo(1);
    }
}


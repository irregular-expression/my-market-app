package ru.irrexp.practicum.mymarket.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import ru.irrexp.practicum.mymarket.dto.OrderDto;
import ru.irrexp.practicum.mymarket.entity.CartItem;
import ru.irrexp.practicum.mymarket.entity.Item;
import ru.irrexp.practicum.mymarket.entity.Order;
import ru.irrexp.practicum.mymarket.entity.OrderItem;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class OrderMapperTest {

    @Autowired
    private OrderMapper orderMapper;

    @Test
    void toDto_mapsOrderFieldsCorrectly() {
        Order order = new Order();
        order.setId(123L);
        order.setTotalSum(5000L);

        OrderDto dto = orderMapper.toDto(order);

        assertThat(dto.id()).isEqualTo(123L);
        assertThat(dto.totalSum()).isEqualTo(5000L);
    }

    @Test
    void toDto_usesOrderItemMapperForItems() {
        Order order = new Order();
        order.setId(456L);
        order.setTotalSum(3000L);

        Item item = new Item();
        item.setId(1L);
        item.setPrice(1000L);

        CartItem cartItem = new CartItem();
        cartItem.setItem(item);
        cartItem.setCount(3);

        OrderItem orderItem = new OrderItem();
        orderItem.setPrice(1000L);
        orderItem.setCount(3);
        orderItem.setOrder(order);

        order.setItems(List.of(orderItem));

        OrderDto dto = orderMapper.toDto(order);

        assertThat(dto.id()).isEqualTo(456L);
        assertThat(dto.totalSum()).isEqualTo(3000L);
        assertThat(dto.items()).hasSize(1);
    }
}

package ru.irrexp.practicum.mymarket.mapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.irrexp.practicum.mymarket.dto.OrderDto;
import ru.irrexp.practicum.mymarket.entity.Item;
import ru.irrexp.practicum.mymarket.entity.Order;
import ru.irrexp.practicum.mymarket.entity.OrderItem;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {OrderMapperImpl.class, OrderItemMapperImpl.class})
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
        item.setTitle("title");
        item.setDescription("description");
        item.setImgPath("/img.jpg");
        item.setPrice(1000L);

        OrderItem orderItem = new OrderItem();
        orderItem.setId(2L);
        orderItem.setPrice(1000L);
        orderItem.setItem(item);
        orderItem.setCount(3);
        orderItem.setOrder(order);

        order.setItems(List.of(orderItem));

        OrderDto dto = orderMapper.toDto(order);

        assertThat(dto.id()).isEqualTo(456L);
        assertThat(dto.totalSum()).isEqualTo(3000L);
        assertThat(dto.items()).hasSize(1);
        assertThat(dto.items().getFirst().id()).isEqualTo(2L);
        assertThat(dto.items().getFirst().title()).isEqualTo("title");
        assertThat(dto.items().getFirst().count()).isEqualTo(3);
    }
}

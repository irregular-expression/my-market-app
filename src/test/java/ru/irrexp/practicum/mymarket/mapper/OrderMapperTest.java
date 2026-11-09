package ru.irrexp.practicum.mymarket.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.irrexp.practicum.mymarket.entity.Item;
import ru.irrexp.practicum.mymarket.entity.Order;
import ru.irrexp.practicum.mymarket.entity.OrderItem;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class OrderMapperTest {

    private final OrderItemMapper orderItemMapper = new OrderItemMapperImpl();
    private final OrderMapper mapper = new OrderMapper(orderItemMapper);

    private Order order(long id, long totalSum) {
        Order order = new Order();
        order.setId(id);
        order.setTotalSum(totalSum);
        return order;
    }

    private OrderItem orderItem(long id, long orderId, long itemId, long price, int count) {
        OrderItem orderItem = new OrderItem();
        orderItem.setId(id);
        orderItem.setOrderId(orderId);
        orderItem.setItemId(itemId);
        orderItem.setPrice(price);
        orderItem.setCount(count);
        return orderItem;
    }

    private Item item(long id, String title, long price) {
        Item item = new Item();
        item.setId(id);
        item.setTitle(title);
        item.setDescription("Описание " + title);
        item.setImgPath("/img/" + title + ".png");
        item.setPrice(price);
        return item;
    }

    @Test
    @DisplayName("toDtoWithItems — id и сумма из заказа, позиции собираются делегированием")
    void toDtoWithItems_mapsOrderFieldsAndDelegatesItems() {
        Map<Long, Item> itemMap = Map.of(5L, item(5L, "Молоко", 89L));
        Order source = order(1L, 178L);
        List<OrderItem> orderItems = List.of(orderItem(10L, 1L, 5L, 89L, 2));

        var dto = mapper.toDtoWithItems(source, orderItems, itemMap);

        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.totalSum()).isEqualTo(178L);
        assertThat(dto.items()).hasSize(1);
        assertThat(dto.items().get(0).title()).isEqualTo("Молоко");
        assertThat(dto.items().get(0).count()).isEqualTo(2);
    }

    @Test
    @DisplayName("toDtoWithItems — null и пустой список позиций дают пустой список в DTO")
    void toDtoWithItems_nullOrEmptyOrderItems() {
        var source = order(1L, 0L);

        assertThat(mapper.toDtoWithItems(source, null, Map.of()).items()).isEmpty();
        assertThat(mapper.toDtoWithItems(source, List.of(), Map.of()).items()).isEmpty();
    }

    @Test
    @DisplayName("toDtoWithItems — позиция без товара в каталоге отфильтровывается делегированием")
    void toDtoWithItems_missingItem_filtered() {
        Map<Long, Item> itemMap = Map.of();
        Order source = order(1L, 45L);

        var dto = mapper.toDtoWithItems(source, List.of(orderItem(10L, 1L, 99L, 45L, 1)), itemMap);

        assertThat(dto.items()).isEmpty();
        assertThat(dto.totalSum()).isEqualTo(45L);
    }
}


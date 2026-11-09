package ru.irrexp.practicum.mymarket.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.irrexp.practicum.mymarket.entity.CartItem;
import ru.irrexp.practicum.mymarket.entity.Item;
import ru.irrexp.practicum.mymarket.entity.OrderItem;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class OrderItemMapperTest {

    private final OrderItemMapper mapper = new OrderItemMapperImpl();

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

    private OrderItem orderItem(long id, long orderId, long itemId, long price, int count) {
        OrderItem orderItem = new OrderItem();
        orderItem.setId(id);
        orderItem.setOrderId(orderId);
        orderItem.setItemId(itemId);
        orderItem.setPrice(price);
        orderItem.setCount(count);
        return orderItem;
    }

    @Test
    @DisplayName("toOrderItem — itemId и цена из товара, count из корзины, id и orderId не выставляются")
    void toOrderItem_mapsFromBothSources() {
        Item source = item(5L, "Молоко", 89L);
        CartItem cartItem = cartItem(5L, 2);

        OrderItem orderItem = mapper.toOrderItem(cartItem, source);

        assertThat(orderItem.getItemId()).isEqualTo(5L);
        assertThat(orderItem.getPrice()).isEqualTo(89L);
        assertThat(orderItem.getCount()).isEqualTo(2);
        assertThat(orderItem.getId()).isNull();
        assertThat(orderItem.getOrderId()).isNull();
    }

    @Test
    @DisplayName("toOrderItem — оба источника null: возвращается null")
    void toOrderItem_bothNull_returnsNull() {
        assertThat(mapper.toOrderItem(null, null)).isNull();
    }

    @Test
    @DisplayName("toOrderItem — item null: заполняется только count")
    void toOrderItem_itemNull_keepsOnlyCount() {
        OrderItem orderItem = mapper.toOrderItem(cartItem(42L, 3), null);

        assertThat(orderItem.getItemId()).isNull();
        assertThat(orderItem.getCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("mapOrderItemsToItemsInOrderDto — null и пустой список дают пустой результат")
    void mapOrderItemsToItemsInOrderDto_nullOrEmpty() {
        assertThat(mapper.mapOrderItemsToItemsInOrderDto(null, Map.of())).isEmpty();
        assertThat(mapper.mapOrderItemsToItemsInOrderDto(List.of(), Map.of())).isEmpty();
    }

    @Test
    @DisplayName("mapOrderItemsToItemsInOrderDto — поля переносятся из позиции и каталога")
    void mapOrderItemsToItemsInOrderDto_mapsFields() {
        Map<Long, Item> itemMap = Map.of(5L, item(5L, "Молоко", 89L));
        OrderItem source = orderItem(10L, 1L, 5L, 89L, 2);

        var dtos = mapper.mapOrderItemsToItemsInOrderDto(List.of(source), itemMap);

        assertThat(dtos).hasSize(1);
        var dto = dtos.get(0);
        assertThat(dto.id()).isEqualTo(10L);
        assertThat(dto.title()).isEqualTo("Молоко");
        assertThat(dto.price()).isEqualTo(89L);
        assertThat(dto.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("mapOrderItemsToItemsInOrderDto — позиция без товара в каталоге отфильтровывается")
    void mapOrderItemsToItemsInOrderDto_missingItem_filtered() {
        Map<Long, Item> itemMap = Map.of(5L, item(5L, "Молоко", 89L));
        List<OrderItem> orderItems = List.of(
                orderItem(10L, 1L, 5L, 89L, 2),
                orderItem(11L, 1L, 99L, 45L, 1));

        var dtos = mapper.mapOrderItemsToItemsInOrderDto(orderItems, itemMap);

        assertThat(dtos).hasSize(1);
        assertThat(dtos.get(0).id()).isEqualTo(10L);
    }
}


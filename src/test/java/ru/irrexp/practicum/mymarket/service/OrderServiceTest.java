package ru.irrexp.practicum.mymarket.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.irrexp.practicum.mymarket.dto.OrderDetailResponse;
import ru.irrexp.practicum.mymarket.dto.OrderDto;
import ru.irrexp.practicum.mymarket.entity.CartItem;
import ru.irrexp.practicum.mymarket.entity.Item;
import ru.irrexp.practicum.mymarket.entity.Order;
import ru.irrexp.practicum.mymarket.exception.OrderNotFoundException;
import ru.irrexp.practicum.mymarket.exception.UnavailableBuyOperationException;
import ru.irrexp.practicum.mymarket.repository.CartItemRepository;
import ru.irrexp.practicum.mymarket.repository.ItemRepository;
import ru.irrexp.practicum.mymarket.repository.OrderRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @BeforeEach
    void setUp() {
        cartItemRepository.deleteAll();
        orderRepository.deleteAll();
        itemRepository.deleteAll();
    }

    @Test
    void getOrders_returnsAllOrdersAsDtos() {
        Order order1 = new Order();
        order1.setTotalSum(1000L);
        orderRepository.save(order1);

        Order order2 = new Order();
        order2.setTotalSum(2500L);
        orderRepository.save(order2);

        List<OrderDto> orders = orderService.getOrders();
        assertThat(orders).hasSize(2);
        assertThat(orders.get(0).totalSum()).isEqualTo(1000L);
        assertThat(orders.get(1).totalSum()).isEqualTo(2500L);
    }

    @Test
    void getOrder_returnsDetailResponseForExistingOrder() {
        Order order = new Order();
        order.setTotalSum(3200L);
        order = orderRepository.save(order);

        OrderDetailResponse response = orderService.getOrder(order.getId(), true);
        assertThat(response.order().totalSum()).isEqualTo(3200L);
        assertThat(response.newOrder()).isTrue();
    }

    @Test
    void getOrder_throwsWhenOrderNotFound() {
        assertThatThrownBy(() -> orderService.getOrder(9999L, false))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining("Заказ не найден");
    }

    @Test
    void buy_createsOrderAndClearsCart() {
        Item item1 = new Item();
        item1.setTitle("Laptop");
        item1.setPrice(70000L);
        item1 = itemRepository.save(item1);

        Item item2 = new Item();
        item2.setTitle("Mouse");
        item2.setPrice(1500L);
        item2 = itemRepository.save(item2);

        CartItem cart1 = new CartItem();
        cart1.setItem(item1);
        cart1.setCount(1);
        cartItemRepository.save(cart1);

        CartItem cart2 = new CartItem();
        cart2.setItem(item2);
        cart2.setCount(2);
        cartItemRepository.save(cart2);

        long orderId = orderService.buy();
        assertThat(orderId).isGreaterThan(0L);

        List<Order> orders = orderRepository.findAll();
        assertThat(orders).hasSize(1);
        assertThat(orders.getFirst().getId()).isEqualTo(orderId);
        assertThat(orders.getFirst().getTotalSum()).isEqualTo(73000L);

        List<CartItem> cartItems = cartItemRepository.findAll();
        assertThat(cartItems).isEmpty();
    }

    @Test
    void buy_throwsWhenCartIsEmpty() {
        assertThatThrownBy(() -> orderService.buy())
                .isInstanceOf(UnavailableBuyOperationException.class)
                .hasMessageContaining("Корзина пуста");
    }
}


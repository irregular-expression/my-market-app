package ru.irrexp.practicum.mymarket.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.irrexp.practicum.mymarket.entity.CartItem;
import ru.irrexp.practicum.mymarket.entity.Item;
import ru.irrexp.practicum.mymarket.entity.Order;
import ru.irrexp.practicum.mymarket.entity.OrderItem;
import ru.irrexp.practicum.mymarket.exception.OrderNotFoundException;
import ru.irrexp.practicum.mymarket.exception.UnavailableBuyOperationException;
import ru.irrexp.practicum.mymarket.repository.CartItemRepository;
import ru.irrexp.practicum.mymarket.repository.ItemRepository;
import ru.irrexp.practicum.mymarket.repository.OrderItemRepository;
import ru.irrexp.practicum.mymarket.repository.OrderRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @BeforeEach
    void cleanUp() {
        orderItemRepository.deleteAll()
                .then(orderRepository.deleteAll())
                .then(cartItemRepository.deleteAll())
                .then(itemRepository.deleteAll())
                .block();
    }

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
    @DisplayName("buy — пустая корзина: UnavailableBuyOperationException, заказ не создаётся")
    void buy_emptyCart_throws() {
        StepVerifier.create(orderService.buy())
                .expectError(UnavailableBuyOperationException.class)
                .verify();

        assertThat(orderRepository.count().block()).isZero();
    }

    @Test
    @DisplayName("buy — корзина с двумя позициями: заказ, позиции с ценами, корзина очищается")
    void buy_success() {
        Item milk = itemRepository.save(item("Молоко", 89L)).block();
        Item bread = itemRepository.save(item("Хлеб", 45L)).block();
        cartItemRepository.save(cartItem(milk.getId(), 2)).block();
        cartItemRepository.save(cartItem(bread.getId(), 1)).block();

        StepVerifier.create(orderService.buy()
                                    .flatMap(orderId -> Mono.zip(
                                                    orderRepository.findById(orderId),
                                                    orderItemRepository.findByOrderId(orderId).collectList(),
                                                    cartItemRepository.findAll().collectList())
                                            .map(state -> new BuyState(orderId, state.getT1(), state.getT2(), state.getT3()))))
                .assertNext(state -> {
                    assertThat(state.order()).isNotNull();
                    assertThat(state.order().getTotalSum()).isEqualTo(223L);

                    assertThat(state.orderItems()).hasSize(2);
                    assertThat(state.orderItems()).allSatisfy(oi -> assertThat(oi.getOrderId()).isEqualTo(state.orderId()));

                    assertThat(state.remainingCartItems()).isEmpty();
                })
                .verifyComplete();
    }

    private record BuyState(Long orderId, Order order, List<OrderItem> orderItems, List<CartItem> remainingCartItems) {
    }


    @Test
    @DisplayName("getOrders — заказов нет: пустой Flux")
    void getOrders_empty() {
        StepVerifier.create(orderService.getOrders())
                .verifyComplete();
    }

    @Test
    @DisplayName("getOrders — два заказа: количество соответствует, позиции распределяются по заказам")
    void getOrders_withOrders() {
        Item milk = itemRepository.save(item("Молоко", 89L)).block();
        Item bread = itemRepository.save(item("Хлеб", 45L)).block();

        Order first = new Order();
        first.setTotalSum(178L);
        Order second = new Order();
        second.setTotalSum(45L);
        first = orderRepository.save(first).block();
        second = orderRepository.save(second).block();

        OrderItem oi1 = new OrderItem();
        oi1.setOrderId(first.getId());
        oi1.setItemId(milk.getId());
        oi1.setPrice(89L);
        oi1.setCount(2);
        OrderItem oi2 = new OrderItem();
        oi2.setOrderId(second.getId());
        oi2.setItemId(bread.getId());
        oi2.setPrice(45L);
        oi2.setCount(1);
        orderItemRepository.saveAll(List.of(oi1, oi2)).collectList().block();

        StepVerifier.create(orderService.getOrders().collectList())
                .assertNext(orders -> assertThat(orders).hasSize(2))
                .verifyComplete();
    }

    @Test
    @DisplayName("getOrder — существующий заказ с позициями и товарами: ответ собран")
    void getOrder_existing() {
        Item milk = itemRepository.save(item("Молоко", 89L)).block();
        Order order = new Order();
        order.setTotalSum(178L);
        order = orderRepository.save(order).block();
        OrderItem oi = new OrderItem();
        oi.setOrderId(order.getId());
        oi.setItemId(milk.getId());
        oi.setPrice(89L);
        oi.setCount(2);
        orderItemRepository.save(oi).block();

        StepVerifier.create(orderService.getOrder(order.getId(), false))
                .assertNext(response -> {
                    assertThat(response.newOrder()).isFalse();
                    assertThat(response.order()).isNotNull();
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("getOrder — newOrder=true передаётся в ответ без изменений")
    void getOrder_newOrderFlagPreserved() {
        Item milk = itemRepository.save(item("Молоко", 89L)).block();
        Order order = new Order();
        order.setTotalSum(89L);
        order = orderRepository.save(order).block();
        OrderItem oi = new OrderItem();
        oi.setOrderId(order.getId());
        oi.setItemId(milk.getId());
        oi.setPrice(89L);
        oi.setCount(1);
        orderItemRepository.save(oi).block();

        StepVerifier.create(orderService.getOrder(order.getId(), true))
                .assertNext(response -> assertThat(response.newOrder()).isTrue())
                .verifyComplete();
    }

    @Test
    @DisplayName("getOrder — несуществующий заказ: OrderNotFoundException")
    void getOrder_missing_throws() {
        StepVerifier.create(orderService.getOrder(4242L, false))
                .expectError(OrderNotFoundException.class)
                .verify();
    }

    @Test
    @DisplayName("getOrder — заказ без позиций: OrderNotFoundException")
    void getOrder_withoutItems_throws() {
        Order order = new Order();
        order.setTotalSum(0L);
        order = orderRepository.save(order).block();

        StepVerifier.create(orderService.getOrder(order.getId(), false))
                .expectError(OrderNotFoundException.class)
                .verify();
    }
}


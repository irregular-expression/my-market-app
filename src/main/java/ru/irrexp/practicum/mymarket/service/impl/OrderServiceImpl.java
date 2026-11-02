package ru.irrexp.practicum.mymarket.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.irrexp.practicum.mymarket.dto.OrderDetailResponse;
import ru.irrexp.practicum.mymarket.dto.OrderDto;
import ru.irrexp.practicum.mymarket.entity.CartItem;
import ru.irrexp.practicum.mymarket.entity.Order;
import ru.irrexp.practicum.mymarket.entity.OrderItem;
import ru.irrexp.practicum.mymarket.mapper.OrderItemMapper;
import ru.irrexp.practicum.mymarket.mapper.OrderMapper;
import ru.irrexp.practicum.mymarket.repository.CartItemRepository;
import ru.irrexp.practicum.mymarket.repository.OrderRepository;
import ru.irrexp.practicum.mymarket.service.OrderService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderItemMapper orderItemMapper;
    private final OrderMapper orderMapper;

    @Override
    @Transactional(readOnly = true)
    public List<OrderDto> getOrders() {
        List<Order> orders = orderRepository.findAll();
        return orders.stream()
                .map(orderMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDetailResponse getOrder(long id, boolean newOrder) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Заказ не найден"));
        OrderDto orderDto = orderMapper.toDto(order);
        return new OrderDetailResponse(orderDto, newOrder);
    }

    @Override
    public long buy() {
        List<CartItem> cartItems = cartItemRepository.findAll();

        if (cartItems.isEmpty()) {
            throw new IllegalStateException("Корзина пуста");
        }

        Order order = new Order();

        List<OrderItem> orderItems = cartItems.stream()
                .map(orderItemMapper::toOrderItem)
                .peek(oi -> oi.setOrder(order))
                .toList();

        long totalSum = orderItems.stream()
                .mapToLong(oi -> oi.getPrice() * oi.getCount())
                .sum();

        order.setTotalSum(totalSum);
        order.setItems(orderItems);

        Order savedOrder = orderRepository.save(order);

        cartItemRepository.deleteAll(cartItems);

        return savedOrder.getId();
    }
}


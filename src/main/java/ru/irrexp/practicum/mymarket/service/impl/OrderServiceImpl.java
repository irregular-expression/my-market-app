package ru.irrexp.practicum.mymarket.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.irrexp.practicum.mymarket.dto.OrderDetailResponse;
import ru.irrexp.practicum.mymarket.dto.OrderDto;
import ru.irrexp.practicum.mymarket.entity.CartItem;
import ru.irrexp.practicum.mymarket.entity.Item;
import ru.irrexp.practicum.mymarket.entity.Order;
import ru.irrexp.practicum.mymarket.entity.OrderItem;
import ru.irrexp.practicum.mymarket.exception.ItemNotFoundException;
import ru.irrexp.practicum.mymarket.exception.OrderNotFoundException;
import ru.irrexp.practicum.mymarket.exception.UnavailableBuyOperationException;
import ru.irrexp.practicum.mymarket.mapper.OrderItemMapper;
import ru.irrexp.practicum.mymarket.mapper.OrderMapper;
import ru.irrexp.practicum.mymarket.repository.CartItemRepository;
import ru.irrexp.practicum.mymarket.repository.ItemRepository;
import ru.irrexp.practicum.mymarket.repository.OrderItemRepository;
import ru.irrexp.practicum.mymarket.repository.OrderRepository;
import ru.irrexp.practicum.mymarket.service.OrderService;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderItemRepository orderItemRepository;
    private final ItemRepository itemRepository;
    private final OrderItemMapper orderItemMapper;
    private final OrderMapper orderMapper;

    @Override
    @Transactional(readOnly = true)
    public Flux<OrderDto> getOrders() {
        return orderRepository.findAll()
                .collectList()
                .flatMapMany(this::buildOrderDtos);
    }

    private Flux<OrderDto> buildOrderDtos(List<Order> orders) {
        if (orders.isEmpty()) {
            return Flux.empty();
        }
        return orderItemRepository.findAll()
                .collectList()
                .flatMapMany(orderItems -> buildOrderDtoWithItems(orders, groupByOrderId(orderItems)));
    }

    private Map<Long, List<OrderItem>> groupByOrderId(List<OrderItem> orderItems) {
        return orderItems.stream()
                .collect(Collectors.groupingBy(OrderItem::getOrderId));
    }

    private Flux<OrderDto> buildOrderDtoWithItems(List<Order> orders, Map<Long, List<OrderItem>> itemsByOrderId) {
        List<Long> itemIds = itemsByOrderId.values().stream()
                .flatMap(List::stream)
                .map(OrderItem::getItemId)
                .toList();

        return itemRepository.findAllById(itemIds)
                .collectMap(Item::getId, Function.identity())
                .flatMapMany(itemMap -> Flux.fromIterable(orders)
                        .map(order -> orderMapper.toDtoWithItems(order, itemsByOrderId.getOrDefault(order.getId(), List.of()), itemMap)));
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<OrderDetailResponse> getOrder(long id, boolean newOrder) {
        return orderRepository.findById(id)
                .switchIfEmpty(Mono.error(new OrderNotFoundException("Заказ не найден")))
                .flatMap(order -> orderItemRepository.findByOrderId(order.getId())
                        .collectList()
                        .flatMap(this::requireOrderItems)
                        .flatMap(orderItems -> loadItemMap(orderItems)
                                .map(itemMap -> new OrderDetailResponse(orderMapper.toDtoWithItems(order, orderItems, itemMap), newOrder))));
    }

    private Mono<List<OrderItem>> requireOrderItems(List<OrderItem> orderItems) {
        return orderItems == null || orderItems.isEmpty()
                ? Mono.error(new OrderNotFoundException("В заказе отсутствуют позиции"))
                : Mono.just(orderItems);
    }

    private Mono<Map<Long, Item>> loadItemMap(List<OrderItem> orderItems) {
        List<Long> itemIds = orderItems.stream()
                .map(OrderItem::getItemId)
                .distinct()
                .toList();

        return itemRepository.findAllById(itemIds)
                .collectMap(Item::getId, Function.identity())
                .flatMap(itemMap -> requireItems(itemIds, itemMap));
    }

    private Mono<Map<Long, Item>> requireItems(List<Long> itemIds, Map<Long, Item> itemMap) {
        return itemIds.stream().allMatch(itemMap::containsKey)
                ? Mono.just(itemMap)
                : Mono.error(new ItemNotFoundException("Заказанный товар отсутствует в каталоге"));
    }

    @Override
    @Transactional
    public Mono<Long> buy() {
        return cartItemRepository.findAll()
                .collectList()
                .flatMap(cartItems -> {
                    if (cartItems.isEmpty()) {
                        return Mono.error(new UnavailableBuyOperationException("Корзина пуста"));
                    }

                    List<Long> itemIds = cartItems.stream()
                            .map(CartItem::getItemId)
                            .toList();

                    return itemRepository.findAllById(itemIds)
                            .collectMap(Item::getId, Function.identity())
                            .flatMap(itemMap -> {
                                List<OrderItem> orderItems = cartItems.stream()
                                        .map(ci -> {
                                            Item item = itemMap.get(ci.getItemId());
                                            return orderItemMapper.toOrderItem(ci, item);
                                        })
                                        .toList();

                                long totalSum = orderItems.stream()
                                        .mapToLong(oi -> oi.getPrice() * oi.getCount())
                                        .sum();

                                Order order = new Order();
                                order.setTotalSum(totalSum);

                                return orderRepository.save(order)
                                        .flatMap(savedOrder -> {
                                            for (OrderItem oi : orderItems) {
                                                oi.setOrderId(savedOrder.getId());
                                            }
                                            return Flux.fromIterable(orderItems)
                                                    .flatMap(orderItemRepository::save)
                                                    .then(Mono.just(savedOrder));
                                        })
                                        .flatMap(savedOrder -> cartItemRepository.deleteAll(cartItems)
                                                .thenReturn(savedOrder.getId()));
                            });
                });
    }
}


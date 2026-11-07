package ru.irrexp.practicum.mymarket.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.irrexp.practicum.mymarket.entity.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

}


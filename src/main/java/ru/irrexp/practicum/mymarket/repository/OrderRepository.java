package ru.irrexp.practicum.mymarket.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.irrexp.practicum.mymarket.entity.Order;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

}


package ru.irrexp.practicum.mymarket.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import ru.irrexp.practicum.mymarket.entity.Order;

@Repository
public interface OrderRepository extends ReactiveCrudRepository<Order, Long> {

}


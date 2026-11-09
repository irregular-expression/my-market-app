package ru.irrexp.practicum.mymarket.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import ru.irrexp.practicum.mymarket.entity.CartItem;
import reactor.core.publisher.Mono;

@Repository
public interface CartItemRepository extends ReactiveCrudRepository<CartItem, Long> {

    Mono<CartItem> findByItemId(Long itemId);

}

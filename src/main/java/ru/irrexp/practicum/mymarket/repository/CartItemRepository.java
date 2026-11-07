package ru.irrexp.practicum.mymarket.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.irrexp.practicum.mymarket.entity.CartItem;

import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByItemId(Long itemId);

}

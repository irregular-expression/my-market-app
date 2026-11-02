package ru.irrexp.practicum.mymarket.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.irrexp.practicum.mymarket.entity.Item;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {

    List<Item> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            String search, String search2);

}


package ru.irrexp.practicum.mymarket.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import ru.irrexp.practicum.mymarket.entity.Item;

@Repository
public interface ItemRepository extends ReactiveCrudRepository<Item, Long> {

    Flux<Item> findAllBy(Pageable pageable);

    Flux<Item> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            String titleSearch,
            String descriptionSearch,
            Pageable pageable
    );
}



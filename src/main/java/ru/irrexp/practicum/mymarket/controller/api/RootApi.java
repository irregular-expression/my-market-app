package ru.irrexp.practicum.mymarket.controller.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;
import ru.irrexp.practicum.mymarket.enums.SortType;

public interface RootApi {

    @GetMapping
    Mono<Rendering> getItems(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "NO") SortType sort,
            @RequestParam(defaultValue = "1") int pageNumber,
            @RequestParam(defaultValue = "5") int pageSize
    );

    @PostMapping("/buy")
    Mono<Rendering> buy();

}


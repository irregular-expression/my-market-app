package ru.irrexp.practicum.mymarket.controller.api;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.result.view.Rendering;
import ru.irrexp.practicum.mymarket.dto.ActionRequest;
import ru.irrexp.practicum.mymarket.dto.ItemCartRequest;
import ru.irrexp.practicum.mymarket.enums.ActionType;
import ru.irrexp.practicum.mymarket.enums.SortType;
import reactor.core.publisher.Mono;


@RequestMapping("/items")
public interface ItemsApi {

    @PostMapping
    Mono<Rendering> updateCart(
            @ModelAttribute ItemCartRequest itemCartRequest
            );

    @GetMapping("/{id}")
    Mono<Rendering> getItem(@PathVariable long id);

    @PostMapping("/{id}")
    Mono<Rendering> updateCartFromItemPage(
            @PathVariable long id,
            @ModelAttribute ActionRequest actionRequest
            );

    @GetMapping
    Mono<Rendering> getItems(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "NO") SortType sort,
            @RequestParam(defaultValue = "1") int pageNumber,
            @RequestParam(defaultValue = "5") int pageSize
    );
}



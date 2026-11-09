package ru.irrexp.practicum.mymarket.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;
import ru.irrexp.practicum.mymarket.controller.api.ItemsApi;
import ru.irrexp.practicum.mymarket.dto.ActionRequest;
import ru.irrexp.practicum.mymarket.dto.ItemCartRequest;
import ru.irrexp.practicum.mymarket.enums.SortType;
import ru.irrexp.practicum.mymarket.service.ItemService;

@Controller
@RequiredArgsConstructor
public class ItemsController implements ItemsApi {

    private final ItemService itemsService;

    @Override
    public Mono<Rendering> getItems(String search, SortType sort, int pageNumber, int pageSize) {
        return itemsService.getItems(search, sort, pageNumber, pageSize)
                .map(result -> Rendering.view("items")
                        .modelAttribute("items", result.items())
                        .modelAttribute("search", result.search())
                        .modelAttribute("sort", result.sort())
                        .modelAttribute("paging", result.paging())
                        .build());
    }

    @Override
    public Mono<Rendering> updateCart(ItemCartRequest itemCartRequest) {
        String redirectUrl = "/items";
        if (itemCartRequest.search() != null) {
            redirectUrl += "?search=" + itemCartRequest.search()
                    + "&sort=" + itemCartRequest.sort()
                    + "&pageNumber=" + itemCartRequest.pageNumber()
                    + "&pageSize=" + itemCartRequest.pageSize();
        } else {
            redirectUrl += "?sort=" + itemCartRequest.sort()
                    + "&pageNumber=" + itemCartRequest.pageNumber()
                    + "&pageSize=" + itemCartRequest.pageSize();
        }
        return itemsService.updateCart(itemCartRequest.id(), itemCartRequest.action())
                .then(Mono.just(Rendering.redirectTo(redirectUrl).build()));

    }

    @Override
    public Mono<Rendering> getItem(long id) {
        return itemsService.getItem(id)
                .map(dto -> Rendering.view("item")
                        .modelAttribute("item", dto)
                        .build());
    }

    @Override
    public Mono<Rendering> updateCartFromItemPage(long id, ActionRequest actionRequest) {
        return itemsService.updateCart(id, actionRequest.action())
                .then(Mono.just(Rendering.redirectTo("/items/" + id).build()));
    }
}




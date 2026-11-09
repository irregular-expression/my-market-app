package ru.irrexp.practicum.mymarket.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;
import ru.irrexp.practicum.mymarket.controller.api.RootApi;
import ru.irrexp.practicum.mymarket.enums.SortType;
import ru.irrexp.practicum.mymarket.service.ItemService;
import ru.irrexp.practicum.mymarket.service.OrderService;

@Controller
@RequiredArgsConstructor
public class RootController implements RootApi {

    private final ItemService itemsService;
    private final OrderService ordersService;

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
    public Mono<Rendering> buy() {
        return ordersService.buy()
                .map(id -> Rendering.redirectTo("/orders/" + id + "?newOrder=true").build());
    }
}


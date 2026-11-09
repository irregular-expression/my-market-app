package ru.irrexp.practicum.mymarket.service;

import reactor.core.publisher.Mono;
import ru.irrexp.practicum.mymarket.dto.ItemDto;
import ru.irrexp.practicum.mymarket.dto.ItemsPageResponse;
import ru.irrexp.practicum.mymarket.enums.ActionType;
import ru.irrexp.practicum.mymarket.enums.SortType;

public interface ItemService {

    Mono<ItemsPageResponse> getItems(String search, SortType sort, int pageNumber, int pageSize);

    Mono<ItemDto> getItem(long id);

    Mono<ItemDto> updateCart(long id, ActionType action);

}


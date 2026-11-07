package ru.irrexp.practicum.mymarket.service;

import ru.irrexp.practicum.mymarket.dto.ItemDto;
import ru.irrexp.practicum.mymarket.dto.ItemsPageResponse;
import ru.irrexp.practicum.mymarket.enums.ActionType;
import ru.irrexp.practicum.mymarket.enums.SortType;

public interface ItemService {

    ItemsPageResponse getItems(String search, SortType sort, int pageNumber, int pageSize);

    ItemDto getItem(long id);

    ItemDto updateCartFromItemPage(long id, ActionType action);

    void updateCart(long id, String search, SortType sort, int pageNumber, int pageSize, ActionType action);

}


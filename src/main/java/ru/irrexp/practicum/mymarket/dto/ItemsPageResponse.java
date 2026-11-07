package ru.irrexp.practicum.mymarket.dto;

import ru.irrexp.practicum.mymarket.enums.SortType;

import java.util.List;

public record ItemsPageResponse(
        List<List<ItemDto>> items,
        String search,
        SortType sort,
        PagingInfo paging
) {}

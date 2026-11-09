package ru.irrexp.practicum.mymarket.dto;

import ru.irrexp.practicum.mymarket.enums.ActionType;
import ru.irrexp.practicum.mymarket.enums.SortType;

public record ItemCartRequest(
        long id,
        String search,
        SortType sort,
        int pageNumber,
        int pageSize,
        ActionType action
) {
    public ItemCartRequest {
        if (sort == null) {
            sort = SortType.NO;
        }
        if (pageNumber <= 0) {
            pageNumber = 1;
        }
        if (pageSize <= 0) {
            pageSize = 5;
        }
    }
}


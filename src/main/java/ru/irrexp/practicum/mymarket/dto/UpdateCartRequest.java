package ru.irrexp.practicum.mymarket.dto;

import ru.irrexp.practicum.mymarket.enums.ActionType;
import ru.irrexp.practicum.mymarket.enums.SortType;

public record UpdateCartRequest(
        long id,
        ActionType action
) {
}


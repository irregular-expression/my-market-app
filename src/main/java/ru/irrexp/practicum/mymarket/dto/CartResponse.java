package ru.irrexp.practicum.mymarket.dto;

import java.util.List;

public record CartResponse(
        List<ItemDto> items,
        long total
) {}

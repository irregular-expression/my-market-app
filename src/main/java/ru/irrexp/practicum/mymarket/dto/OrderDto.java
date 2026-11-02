package ru.irrexp.practicum.mymarket.dto;

import java.util.List;

public record OrderDto(
        long id,
        List<ItemInOrderDto> items,
        long totalSum
) {}

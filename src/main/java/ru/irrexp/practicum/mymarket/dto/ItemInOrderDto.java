package ru.irrexp.practicum.mymarket.dto;

public record ItemInOrderDto(
        long id,
        String title,
        long price,
        int count
) {}

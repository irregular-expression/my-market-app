package ru.irrexp.practicum.mymarket.dto;

public record ItemDto(
        long id,
        String title,
        String description,
        String imgPath,
        long price,
        int count
) {}

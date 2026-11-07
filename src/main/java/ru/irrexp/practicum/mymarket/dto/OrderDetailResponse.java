package ru.irrexp.practicum.mymarket.dto;

public record OrderDetailResponse(
        OrderDto order,
        boolean newOrder
) {}

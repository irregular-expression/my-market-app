package ru.irrexp.practicum.mymarket.dto;

public record PagingInfo(
        int pageSize,
        int pageNumber,
        boolean hasPrevious,
        boolean hasNext
) {}

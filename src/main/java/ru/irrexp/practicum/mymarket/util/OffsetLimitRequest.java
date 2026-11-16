package ru.irrexp.practicum.mymarket.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

/**
 * Кастомный PageRequest для корректной работы hasNext и offset в пагинации
 */
public class OffsetLimitRequest extends PageRequest {

    private final long offset;

    private OffsetLimitRequest(int pageNumber, int fetchSize, long offset, Sort sort) {
        super(pageNumber, fetchSize, sort);
        this.offset = offset;
    }

    public static OffsetLimitRequest of(int pageNumber, int fetchSize, long offset, Sort sort) {
        return new OffsetLimitRequest(pageNumber, fetchSize, offset, sort);
    }

    @Override
    public long getOffset() {
        return offset;
    }
}


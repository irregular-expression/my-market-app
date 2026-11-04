package ru.irrexp.practicum.mymarket.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.irrexp.practicum.mymarket.dto.ErrorResponse;
import ru.irrexp.practicum.mymarket.exception.IncorrectParamException;
import ru.irrexp.practicum.mymarket.exception.ItemNotFoundException;
import ru.irrexp.practicum.mymarket.exception.OrderNotFoundException;
import ru.irrexp.practicum.mymarket.exception.UnavailableBuyOperationException;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ItemNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleItemNotFound(ItemNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleOrderNotFound(OrderNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(IncorrectParamException.class)
    public ResponseEntity<ErrorResponse> handleIncorrectParam(IncorrectParamException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(UnavailableBuyOperationException.class)
    public ResponseEntity<ErrorResponse> handleUnavailableBuy(UnavailableBuyOperationException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> catchException(Exception e) {
        log.error("Unexpected error", e);
        return ResponseEntity.internalServerError().body(new ErrorResponse(e.getMessage()));
    }
}


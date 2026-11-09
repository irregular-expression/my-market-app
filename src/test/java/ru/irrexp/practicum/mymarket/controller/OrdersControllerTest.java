package ru.irrexp.practicum.mymarket.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.thymeleaf.autoconfigure.ThymeleafAutoConfiguration;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.irrexp.practicum.mymarket.dto.OrderDetailResponse;
import ru.irrexp.practicum.mymarket.dto.OrderDto;
import ru.irrexp.practicum.mymarket.exception.OrderNotFoundException;
import ru.irrexp.practicum.mymarket.service.OrderService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebFluxTest(OrdersController.class)
@Import({ThymeleafAutoConfiguration.class, GlobalExceptionHandler.class})
class OrdersControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private OrderService ordersService;

    private static OrderDto order(long id) {
        return new OrderDto(id, List.of(), 0L);
    }

    @Test
    @DisplayName("GET /orders — заказов нет: страница заказов с 200")
    void getOrders_empty() {
        when(ordersService.getOrders()).thenReturn(Flux.empty());

        webTestClient.get().uri("/orders")
                .exchange()
                .expectStatus().isOk();

        verify(ordersService).getOrders();
    }

    @Test
    @DisplayName("GET /orders — есть заказы: список собирается через collectList")
    void getOrders_withOrders() {
        when(ordersService.getOrders()).thenReturn(Flux.just(order(1L), order(2L)));

        webTestClient.get().uri("/orders")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(body -> assertThat(body).isNotBlank());

        verify(ordersService).getOrders();
    }

    @Test
    @DisplayName("GET /orders/{id} — newOrder по умолчанию false, сервис получает false")
    void getOrder_defaultNewOrderFalse() {
        when(ordersService.getOrder(1L, false))
                .thenReturn(Mono.just(new OrderDetailResponse(order(1L), false)));

        webTestClient.get().uri("/orders/1")
                .exchange()
                .expectStatus().isOk();

        verify(ordersService).getOrder(1L, false);
    }

    @Test
    @DisplayName("GET /orders/{id}?newOrder=true — флаг передаётся в сервис")
    void getOrder_newOrderTrue() {
        when(ordersService.getOrder(7L, true))
                .thenReturn(Mono.just(new OrderDetailResponse(order(7L), true)));

        webTestClient.get()
                .uri(b -> b.path("/orders/7").queryParam("newOrder", true).build())
                .exchange()
                .expectStatus().isOk();

        verify(ordersService).getOrder(7L, true);
    }

    @Test
    @DisplayName("GET /orders/{id} — заказ не найден: 404 с сообщением об ошибке")
    void getOrder_missingOrder_returns404() {
        when(ordersService.getOrder(42L, false))
                .thenReturn(Mono.error(new OrderNotFoundException("Заказ не найден")));

        webTestClient.get().uri("/orders/42")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Заказ не найден");
    }
}


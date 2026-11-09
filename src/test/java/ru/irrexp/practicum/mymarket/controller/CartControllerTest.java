package ru.irrexp.practicum.mymarket.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.thymeleaf.autoconfigure.ThymeleafAutoConfiguration;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import ru.irrexp.practicum.mymarket.dto.CartResponse;
import ru.irrexp.practicum.mymarket.dto.ItemDto;
import ru.irrexp.practicum.mymarket.enums.ActionType;
import ru.irrexp.practicum.mymarket.exception.ItemNotFoundException;
import ru.irrexp.practicum.mymarket.service.CartService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebFluxTest(CartController.class)
@Import(ThymeleafAutoConfiguration.class)
class CartControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private CartService cartService;

    private static ItemDto item(long id, String title, long price, int count) {
        return new ItemDto(id, title, "Описание " + title, "/img/" + title + ".png", price, count);
    }

    @Test
    @DisplayName("GET /cart/items — пустая корзина: страница корзины отдаётся с 200")
    void getCartItems_emptyCart() {
        when(cartService.getCart())
                .thenReturn(Mono.just(new CartResponse(List.of(), 0L)));

        webTestClient.get().uri("/cart/items")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML);
    }

    @Test
    @DisplayName("GET /cart/items — непустая корзина: товары и сумма попадают в модель")
    void getCartItems_withItems() {
        List<ItemDto> items = List.of(
                item(1L, "Молоко", 89L, 2),
                item(2L, "Хлеб", 45L, 1)
        );
        when(cartService.getCart())
                .thenReturn(Mono.just(new CartResponse(items, 223L)));

        webTestClient.get().uri("/cart/items")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
                .expectBody(String.class)
                .value(body -> {
                    assertThat(body).contains("Молоко");
                    assertThat(body).contains("Хлеб");
                });

        verify(cartService).getCart();
    }

    @Test
    @DisplayName("POST /cart/items — валидный запрос: редирект обратно на корзину")
    void updateCartItem_validRequest_redirectsToCart() {
        when(cartService.updateCartItem(1L, ActionType.PLUS))
                .thenReturn(Mono.empty());

        webTestClient.post()
                .uri(b -> b.path("/cart/items")
                        .queryParam("id", 1L)
                        .queryParam("action", "PLUS")
                        .build())
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/cart/items");

        verify(cartService).updateCartItem(1L, ActionType.PLUS);
    }

    @Test
    @DisplayName("POST /cart/items — ошибка сервиса (товар не найден): пробрасывается наверх как 500")
    void updateCartItem_serviceError_propagates() {
        when(cartService.updateCartItem(99L, ActionType.MINUS))
                .thenReturn(Mono.error(new ItemNotFoundException("Товар не найден")));

        webTestClient.post()
                .uri(b -> b.path("/cart/items")
                        .queryParam("id", 99L)
                        .queryParam("action", "MINUS")
                        .build())
                .exchange()
                .expectStatus().isNotFound();
    }
}


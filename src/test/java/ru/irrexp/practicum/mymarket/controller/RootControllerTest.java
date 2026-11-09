package ru.irrexp.practicum.mymarket.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.thymeleaf.autoconfigure.ThymeleafAutoConfiguration;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import ru.irrexp.practicum.mymarket.dto.ItemDto;
import ru.irrexp.practicum.mymarket.dto.ItemsPageResponse;
import ru.irrexp.practicum.mymarket.dto.PagingInfo;
import ru.irrexp.practicum.mymarket.enums.SortType;
import ru.irrexp.practicum.mymarket.exception.UnavailableBuyOperationException;
import ru.irrexp.practicum.mymarket.service.ItemService;
import ru.irrexp.practicum.mymarket.service.OrderService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebFluxTest(RootController.class)
@Import({ThymeleafAutoConfiguration.class, GlobalExceptionHandler.class})
class RootControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private ItemService itemsService;

    @MockitoBean
    private OrderService ordersService;

    private static ItemDto item(long id, String title, long price, int count) {
        return new ItemDto(id, title, "Описание " + title, "/img/" + title + ".png", price, count);
    }

    @Test
    @DisplayName("GET / — главная страница каталога: 200 и товары в модели")
    void getItems_firstPage() {
        List<ItemDto> items = List.of(item(1L, "Молоко", 89L, 0));
        when(itemsService.getItems(null, SortType.NO, 1, 5))
                .thenReturn(Mono.just(new ItemsPageResponse(
                        List.of(items), null, SortType.NO,
                        new PagingInfo(5, 1, false, false))));

        webTestClient.get().uri("/")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(body -> assertThat(body).contains("Молоко"));

        verify(itemsService).getItems(null, SortType.NO, 1, 5);
    }

    @Test
    @DisplayName("GET / — параметры поиска и пагинации пробрасываются в сервис")
    void getItems_forwardsParams() {
        when(itemsService.getItems("milk", SortType.PRICE, 2, 3))
                .thenReturn(Mono.just(new ItemsPageResponse(
                        List.of(), "milk", SortType.PRICE,
                        new PagingInfo(3, 2, true, false))));

        webTestClient.get()
                .uri(b -> b.path("/")
                        .queryParam("search", "milk")
                        .queryParam("sort", "PRICE")
                        .queryParam("pageNumber", 2)
                        .queryParam("pageSize", 3)
                        .build())
                .exchange()
                .expectStatus().isOk();

        verify(itemsService).getItems("milk", SortType.PRICE, 2, 3);
    }

    @Test
    @DisplayName("POST /buy — успешная покупка: редирект на новый заказ с newOrder=true")
    void buy_success_redirectsToNewOrder() {
        when(ordersService.buy()).thenReturn(Mono.just(7L));

        webTestClient.post().uri("/buy")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/orders/7?newOrder=true");

        verify(ordersService).buy();
    }

    @Test
    @DisplayName("POST /buy — пустая корзина: 409 с сообщением об ошибке")
    void buy_emptyCart_returns409() {
        when(ordersService.buy())
                .thenReturn(Mono.error(new UnavailableBuyOperationException("Корзина пуста")));

        webTestClient.post().uri("/buy")
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Корзина пуста");
    }
}


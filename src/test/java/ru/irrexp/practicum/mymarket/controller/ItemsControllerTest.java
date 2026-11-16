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
import ru.irrexp.practicum.mymarket.enums.ActionType;
import ru.irrexp.practicum.mymarket.enums.SortType;
import ru.irrexp.practicum.mymarket.exception.ItemNotFoundException;
import ru.irrexp.practicum.mymarket.service.ItemService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebFluxTest(ItemsController.class)
@Import({ThymeleafAutoConfiguration.class, GlobalExceptionHandler.class})
class ItemsControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private ItemService itemsService;

    private static ItemDto item(long id, String title, long price, int count) {
        return new ItemDto(id, title, "Описание " + title, "/img/" + title + ".png", price, count);
    }

    @Test
    @DisplayName("GET /items — первая страница без поиска: 200 и товары в модели")
    void getItems_firstPageNoSearch() {
        List<ItemDto> items = List.of(item(1L, "Молоко", 89L, 0), item(2L, "Хлеб", 45L, 0));
        when(itemsService.getItems(null, SortType.NO, 1, 5))
                .thenReturn(Mono.just(new ItemsPageResponse(
                        List.of(items), null, SortType.NO,
                        new PagingInfo(5, 1, false, false))));

        webTestClient.get().uri("/items")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(body -> assertThat(body).contains("Молоко"));

        verify(itemsService).getItems(null, SortType.NO, 1, 5);
    }

    @Test
    @DisplayName("GET /items — параметры поиска, сортировки и пагинации передаются в сервис")
    void getItems_passesAllParamsToService() {
        List<ItemDto> items = List.of(item(3L, "Молоко", 89L, 0));
        when(itemsService.getItems("молоко", SortType.PRICE, 2, 3))
                .thenReturn(Mono.just(new ItemsPageResponse(
                        List.of(items), "молоко", SortType.PRICE,
                        new PagingInfo(3, 2, true, false))));

        webTestClient.get()
                .uri(b -> b.path("/items")
                        .queryParam("search", "молоко")
                        .queryParam("sort", "PRICE")
                        .queryParam("pageNumber", 2)
                        .queryParam("pageSize", 3)
                        .build())
                .exchange()
                .expectStatus().isOk();

        verify(itemsService).getItems("молоко", SortType.PRICE, 2, 3);
    }

    @Test
    @DisplayName("GET /items/{id} — существующий товар: страница товара с 200")
    void getItem_existingItem() {
        when(itemsService.getItem(1L)).thenReturn(Mono.just(item(1L, "Молоко", 89L, 0)));

        webTestClient.get().uri("/items/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(body -> assertThat(body).contains("Молоко"));

        verify(itemsService).getItem(1L);
    }

    @Test
    @DisplayName("GET /items/{id} — несуществующий товар: 404 с сообщением об ошибке")
    void getItem_missingItem_returns404() {
        when(itemsService.getItem(42L))
                .thenReturn(Mono.error(new ItemNotFoundException("Товар не найден")));

        webTestClient.get().uri("/items/42")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Товар не найден");
    }

    @Test
    @DisplayName("POST /items — с search: редирект сохраняет все параметры")
    void updateCart_withSearch_redirectsWithAllParams() {
        when(itemsService.updateCart(1L, ActionType.PLUS)).thenReturn(Mono.empty());

        webTestClient.post()
                .uri(b -> b.path("/items")
                        .queryParam("id", 1)
                        .queryParam("search", "milk")
                        .queryParam("sort", "PRICE")
                        .queryParam("pageNumber", 2)
                        .queryParam("pageSize", 3)
                        .queryParam("action", "PLUS")
                        .build())
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/items?search=milk&sort=PRICE&pageNumber=2&pageSize=3");

        verify(itemsService).updateCart(1L, ActionType.PLUS);
    }

    @Test
    @DisplayName("POST /items — без search: редирект без параметра search")
    void updateCart_withoutSearch_redirectsWithoutSearch() {
        when(itemsService.updateCart(1L, ActionType.MINUS)).thenReturn(Mono.empty());

        webTestClient.post()
                .uri(b -> b.path("/items")
                        .queryParam("id", 1)
                        .queryParam("sort", "NO")
                        .queryParam("pageNumber", 1)
                        .queryParam("pageSize", 5)
                        .queryParam("action", "MINUS")
                        .build())
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/items?sort=NO&pageNumber=1&pageSize=5");
    }

    @Test
    @DisplayName("POST /items/{id} — редирект на страницу этого товара")
    void updateCartFromItemPage_redirectsToItemPage() {
        when(itemsService.updateCart(1L, ActionType.PLUS)).thenReturn(Mono.empty());

        webTestClient.post()
                .uri(b -> b.path("/items/1")
                        .queryParam("action", "PLUS")
                        .build())
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/items/1");

        verify(itemsService).updateCart(1L, ActionType.PLUS);
    }
}


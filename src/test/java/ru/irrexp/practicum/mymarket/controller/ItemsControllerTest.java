package ru.irrexp.practicum.mymarket.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import ru.irrexp.practicum.mymarket.dto.ItemDto;
import ru.irrexp.practicum.mymarket.dto.ItemsPageResponse;
import ru.irrexp.practicum.mymarket.dto.PagingInfo;
import ru.irrexp.practicum.mymarket.enums.ActionType;
import ru.irrexp.practicum.mymarket.enums.SortType;
import ru.irrexp.practicum.mymarket.service.ItemService;
import ru.irrexp.practicum.mymarket.service.OrderService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest({RootController.class, ItemsController.class})
class ItemsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ItemService itemService;

    @MockitoBean
    private OrderService orderService;

    @Test
    void listItems_returnsViewWithPagingAndSearch() throws Exception {
        ItemsPageResponse mockResponse = new ItemsPageResponse(
                List.of(List.of()),
                "test",
                SortType.NO,
                new PagingInfo(5, 1, false, false)
        );
        when(itemService.getItems("test", SortType.NO, 1, 5)).thenReturn(mockResponse);

        mockMvc.perform(get("/items")
                                .param("search", "test")
                                .param("sort", "NO")
                                .param("pageNumber", "1")
                                .param("pageSize", "5"))
                .andExpect(status().isOk())
                .andExpect(view().name("items"))
                .andExpect(model().attribute("items", mockResponse.items()))
                .andExpect(model().attribute("search", "test"))
                .andExpect(model().attribute("sort", SortType.NO))
                .andExpect(model().attribute("paging", mockResponse.paging()));
    }

    @Test
    void rootAndItemsList_shareSameLogic() throws Exception {
        ItemsPageResponse mockResponse = new ItemsPageResponse(
                List.of(List.of(new ItemDto(1L, "Товар", "Какой-то товар", "Картинка", 0L, 1))),
                "",
                SortType.PRICE,
                new PagingInfo(10, 2, false, false)
        );
        when(itemService.getItems(null, SortType.PRICE, 2, 10)).thenReturn(mockResponse);

        // Проверка /items
        mockMvc.perform(get("/items")
                                .param("sort", "PRICE")
                                .param("pageNumber", "2")
                                .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(view().name("items"));

        // Проверка корневого /
        mockMvc.perform(get("/")
                                .param("sort", "PRICE")
                                .param("pageNumber", "2")
                                .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(view().name("items"));
    }

    @Test
    void getSingleItem_returnsDetailView() throws Exception {
        ItemDto mockDto = new ItemDto(1L, "Test Phone", "Desc", "/static/test.png", 1000L, 0);
        when(itemService.getItem(1L)).thenReturn(mockDto);

        mockMvc.perform(get("/items/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("item"))
                .andExpect(model().attribute("item", mockDto));
    }

    @Test
    void updateFromDetailPage_returnsDetailViewWithUpdatedDto() throws Exception {
        ItemDto mockDto = new ItemDto(2L, "Updated Phone", "New desc", "/static/upd.png", 2000L, 3);
        when(itemService.updateCartFromItemPage(2L, ActionType.PLUS)).thenReturn(mockDto);

        mockMvc.perform(post("/items/2")
                                .param("action", "PLUS"))
                .andExpect(status().isOk())
                .andExpect(view().name("item"))
                .andExpect(model().attribute("item", mockDto));
    }

    @Test
    void updateFromList_redirectsToItemsWithParams() throws Exception {
        doNothing().when(itemService).updateCart(1L, "phone", SortType.ALPHA, 1, 10, ActionType.MINUS);

        MvcResult result = mockMvc.perform(post("/items")
                                                   .param("id", "1")
                                                   .param("search", "phone")
                                                   .param("sort", "ALPHA")
                                                   .param("pageNumber", "1")
                                                   .param("pageSize", "10")
                                                   .param("action", "MINUS"))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        String redirectUrl = result.getResponse().getRedirectedUrl();
        assertThat(redirectUrl).isEqualTo("/items?search=phone&sort=ALPHA&pageNumber=1&pageSize=10");
    }
}


package ru.irrexp.practicum.mymarket.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.irrexp.practicum.mymarket.dto.CartResponse;
import ru.irrexp.practicum.mymarket.enums.ActionType;
import ru.irrexp.practicum.mymarket.service.CartService;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(CartController.class)
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CartService cartService;

    @Test
    void getCartItems_returnsViewWithAttributes() throws Exception {
        CartResponse mockResponse = new CartResponse(List.of(), 0L);
        when(cartService.getCart()).thenReturn(mockResponse);

        mockMvc.perform(get("/cart/items"))
                .andExpect(status().isOk())
                .andExpect(view().name("cart"))
                .andExpect(model().attribute("items", mockResponse.items()))
                .andExpect(model().attribute("total", mockResponse.total()));
    }

    @Test
    void updateCartItem_returnsViewWithUpdatedAttributes() throws Exception {
        CartResponse mockResponse = new CartResponse(List.of(), 1500L);
        when(cartService.updateCartItem(123L, ActionType.PLUS)).thenReturn(mockResponse);

        mockMvc.perform(post("/cart/items")
                                .param("id", "123")
                                .param("action", "PLUS"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/cart"));
    }
}


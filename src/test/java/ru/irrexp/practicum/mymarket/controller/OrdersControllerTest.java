package ru.irrexp.practicum.mymarket.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.irrexp.practicum.mymarket.dto.OrderDetailResponse;
import ru.irrexp.practicum.mymarket.dto.OrderDto;
import ru.irrexp.practicum.mymarket.service.OrderService;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(OrdersController.class)
class OrdersControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;


    @Test
    void getOrders_shouldReturnViewAndPopulateModel() throws Exception {

        var orders = List.of(
                new OrderDto(1L, List.of(), 0),
                new OrderDto(2L, List.of(), 0)
        );

        when(orderService.getOrders()).thenReturn(orders);

        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(view().name("orders"))
                .andExpect(model().attribute("orders", orders));
    }

    @Test
    void getOrderById_shouldReturnViewAndPopulateModelWithOrderAndNewOrderFlag() throws Exception {
        long id = 42L;
        boolean newOrder = true;

        var dto = new OrderDto(1L, List.of(), 0);

        when(orderService.getOrder(eq(id), eq(newOrder))).thenReturn(new OrderDetailResponse(dto, newOrder));

        mockMvc.perform(get("/orders/{id}", id)
                                .param("newOrder", String.valueOf(newOrder)))
                .andExpect(status().isOk())
                .andExpect(view().name("order"))
                .andExpect(model().attribute("order", dto))
                .andExpect(model().attribute("newOrder", newOrder));
    }

    @Test
    void getOrderById_withDefaultNewOrder_shouldUseFalse() throws Exception {
        long id = 77L;

        var dto = new OrderDto(1L, List.of(), 0);

        when(orderService.getOrder(eq(id), eq(false))).thenReturn(new OrderDetailResponse(dto, false));

        mockMvc.perform(get("/orders/{id}", id))
                .andExpect(status().isOk())
                .andExpect(view().name("order"))
                .andExpect(model().attribute("order", dto))
                .andExpect(model().attribute("newOrder", false));
    }
}

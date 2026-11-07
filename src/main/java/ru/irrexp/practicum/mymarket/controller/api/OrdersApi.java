package ru.irrexp.practicum.mymarket.controller.api;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("/orders")
public interface OrdersApi {

    @GetMapping
    String getOrders(Model model);

    @GetMapping("/{id}")
    String getOrder(
            @PathVariable long id,
            @RequestParam(defaultValue = "false") boolean newOrder,
            Model model
    );

}

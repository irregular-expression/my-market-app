package ru.irrexp.practicum.mymarket.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.irrexp.practicum.mymarket.controller.api.OrdersApi;
import ru.irrexp.practicum.mymarket.service.OrderService;

@Controller
@RequiredArgsConstructor
public class OrdersController implements OrdersApi {

    private final OrderService ordersService;

    @Override
    public String getOrders(Model model) {
        model.addAttribute("orders", ordersService.getOrders());
        return "orders";
    }

    @Override
    public String getOrder(@PathVariable long id,
                           @RequestParam(defaultValue = "false") boolean newOrder,
                           Model model) {
        var dto = ordersService.getOrder(id, newOrder);
        model.addAttribute("order", dto.order());
        model.addAttribute("newOrder", dto.newOrder());
        return "order";
    }
}


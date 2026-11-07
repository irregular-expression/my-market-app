package ru.irrexp.practicum.mymarket.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import ru.irrexp.practicum.mymarket.controller.api.RootApi;
import ru.irrexp.practicum.mymarket.enums.SortType;
import ru.irrexp.practicum.mymarket.service.ItemService;
import ru.irrexp.practicum.mymarket.service.OrderService;

@Controller
@RequiredArgsConstructor
public class RootController implements RootApi {

    private final ItemService itemsService;
    private final OrderService ordersService;

    @Override
    public String getItems(Model model,
                           String search,
                           SortType sort,
                           int pageNumber,
                           int pageSize) {
        var result = itemsService.getItems(search, sort, pageNumber, pageSize);
        model.addAttribute("items", result.items());
        model.addAttribute("search", result.search());
        model.addAttribute("sort", result.sort());
        model.addAttribute("paging", result.paging());
        return "items";
    }

    @Override
    public String buy(Model model) {
        long orderId = ordersService.buy();
        String location = "/orders/" + orderId + "?newOrder=true";
        return "redirect:" + location;
    }

}

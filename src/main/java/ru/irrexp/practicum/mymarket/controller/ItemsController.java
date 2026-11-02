package ru.irrexp.practicum.mymarket.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.util.UriComponentsBuilder;
import ru.irrexp.practicum.mymarket.controller.api.ItemsApi;
import ru.irrexp.practicum.mymarket.enums.ActionType;
import ru.irrexp.practicum.mymarket.enums.SortType;
import ru.irrexp.practicum.mymarket.service.ItemService;

@Controller
@RequiredArgsConstructor
public class ItemsController implements ItemsApi {

    private final ItemService itemsService;

    @Override
    public String getItems(Model model, String search, SortType sort, int pageNumber, int pageSize) {
        var result = itemsService.getItems(search, sort, pageNumber, pageSize);
        model.addAttribute("items", result.items());
        model.addAttribute("search", result.search());
        model.addAttribute("sort", result.sort());
        model.addAttribute("paging", result.paging());
        return "items";
    }

    @Override
    public String updateCart(long id,
                                           String search,
                                           SortType sort,
                                           int pageNumber,
                                           int pageSize,
                                           ActionType action) {
        itemsService.updateCart(id, search, sort, pageNumber, pageSize, action);

        UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/items");
        if (search != null) {
            builder.queryParam("search", search);
        }
        builder.queryParam("sort", sort)
                .queryParam("pageNumber", pageNumber)
                .queryParam("pageSize", pageSize);

        String redirectUrl = builder.build().toUriString();

        return "redirect:" + redirectUrl;
    }


    @Override
    public String getItem(long id, Model model) {
        var dto = itemsService.getItem(id);
        model.addAttribute("item", dto);
        return "item";
    }

    @Override
    public String updateCartFromItemPage(long id, ActionType action, Model model) {
        var dto = itemsService.updateCartFromItemPage(id, action);
        model.addAttribute("item", dto);
        return "item";
    }
}



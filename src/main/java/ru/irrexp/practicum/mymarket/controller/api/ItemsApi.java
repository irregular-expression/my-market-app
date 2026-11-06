package ru.irrexp.practicum.mymarket.controller.api;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.irrexp.practicum.mymarket.enums.ActionType;
import ru.irrexp.practicum.mymarket.enums.SortType;

@RequestMapping("/items")
public interface ItemsApi {

    @PostMapping
    String updateCart(
            @RequestParam long id,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "NO") SortType sort,
            @RequestParam(defaultValue = "1") int pageNumber,
            @RequestParam(defaultValue = "5") int pageSize,
            @RequestParam ActionType action,
            RedirectAttributes redirectAttributes
    );

    @GetMapping("/{id}")
    String getItem(@PathVariable long id, Model model);

    @PostMapping("/{id}")
    String updateCartFromItemPage(
            @PathVariable long id,
            @RequestParam ActionType action
    );

    @GetMapping
    String getItems(Model model,
                    @RequestParam(required = false) String search,
                    @RequestParam(defaultValue = "NO") SortType sort,
                    @RequestParam(defaultValue = "1") int pageNumber,
                    @RequestParam(defaultValue = "5") int pageSize
    );
}


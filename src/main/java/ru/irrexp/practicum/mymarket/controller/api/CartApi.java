package ru.irrexp.practicum.mymarket.controller.api;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.irrexp.practicum.mymarket.enums.ActionType;


@RequestMapping("/cart")
public interface CartApi {

    @GetMapping("/items")
    String getCartItems(Model model);

    @PostMapping("/items")
    String updateCartItem(
            @RequestParam long id,
            @RequestParam ActionType action,
            Model model
    );
}

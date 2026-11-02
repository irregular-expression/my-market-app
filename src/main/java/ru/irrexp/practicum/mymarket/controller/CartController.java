package ru.irrexp.practicum.mymarket.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import ru.irrexp.practicum.mymarket.controller.api.CartApi;
import ru.irrexp.practicum.mymarket.enums.ActionType;
import ru.irrexp.practicum.mymarket.service.CartService;

@Controller
@RequiredArgsConstructor
public class CartController implements CartApi {

    private final CartService cartService;

    @Override
    public String getCartItems(Model model) {
        var dto = cartService.getCart();
        model.addAttribute("items", dto.items());
        model.addAttribute("total", dto.total());
        return "cart";
    }

    @Override
    public String updateCartItem(long id, ActionType action, Model model) {
        var dto = cartService.updateCartItem(id, action);
        model.addAttribute("items", dto.items());
        model.addAttribute("total", dto.total());
        return "cart";
    }
}


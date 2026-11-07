package ru.irrexp.practicum.mymarket.controller.api;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.irrexp.practicum.mymarket.enums.SortType;

public interface RootApi {

    @GetMapping
    String getItems(Model model,
                    @RequestParam(required = false) String search,
                    @RequestParam(defaultValue = "NO") SortType sort,
                    @RequestParam(defaultValue = "1") int pageNumber,
                    @RequestParam(defaultValue = "5") int pageSize
    );

    @PostMapping("/buy")
    String buy(Model model);

}

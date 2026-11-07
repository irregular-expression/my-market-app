package ru.irrexp.practicum.mymarket.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import ru.irrexp.practicum.mymarket.dto.ItemDto;
import ru.irrexp.practicum.mymarket.entity.Item;
import ru.irrexp.practicum.mymarket.repository.CartItemRepository;
import ru.irrexp.practicum.mymarket.repository.ItemRepository;

import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CartConsistencyIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    private Item item;

    @BeforeEach
    void setUp() {
        cartItemRepository.deleteAll();
        itemRepository.deleteAll();

        item = new Item();
        item.setTitle("Integration Test Item");
        item.setDescription("Consistency check");
        item.setImgPath("/static/test.png");
        item.setPrice(500L);
        item = itemRepository.save(item);
    }

    @AfterEach
    void tearDown() {
        cartItemRepository.deleteAll();
        itemRepository.deleteAll();
    }

    @Test
    void countUpdatedThroughItemsDetailIsVisibleInCatalogItemCardAndCart() throws Exception {
        mockMvc.perform(post("/items/" + item.getId())
                                .param("action", "PLUS"))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(post("/items/" + item.getId())
                                .param("action", "PLUS"))
                .andExpect(status().is3xxRedirection());

        MvcResult catalogResult = mockMvc.perform(get("/items")
                                                          .param("search", "Integration")
                                                          .param("sort", "NO")
                                                          .param("pageNumber", "1")
                                                          .param("pageSize", "5"))
                .andExpect(status().isOk())
                .andExpect(view().name("items"))
                .andReturn();

        List<List<ItemDto>> catalogRows = extractModelAttribute(catalogResult, "items");
        ItemDto catalogDto = catalogRows.stream()
                .flatMap(List::stream)
                .filter(i -> item.getId().equals(i.id()))
                .findFirst()
                .orElseThrow();
        assertThat(catalogDto.count()).isEqualTo(2);

        MvcResult itemCardResult = mockMvc.perform(get("/items/" + item.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("item"))
                .andReturn();

        ItemDto cardDto = extractModelAttribute(itemCardResult, "item");
        assertThat(cardDto.id()).isEqualTo(item.getId());
        assertThat(cardDto.count()).isEqualTo(2);

        MvcResult cartResult = mockMvc.perform(get("/cart/items"))
                .andExpect(status().isOk())
                .andExpect(view().name("cart"))
                .andReturn();

        List<ItemDto> cartItems = extractModelAttribute(cartResult, "items");
        Long cartTotal = extractModelAttribute(cartResult, "total");
        assertThat(cartItems).hasSize(1);
        assertThat(cartItems.getFirst().id()).isEqualTo(item.getId());
        assertThat(cartItems.getFirst().count()).isEqualTo(2);
        assertThat(cartTotal).isEqualTo(1000L);
    }

    @Test
    void countUpdatedToZeroDisappearsFromCartAndCatalogShowsZero() throws Exception {
        mockMvc.perform(post("/items/" + item.getId())
                                .param("action", "PLUS"))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(post("/items/" + item.getId())
                                .param("action", "MINUS"))
                .andExpect(status().is3xxRedirection());

        MvcResult itemCardResult = mockMvc.perform(get("/items/" + item.getId()))
                .andExpect(status().isOk())
                .andReturn();

        ItemDto cardDto = extractModelAttribute(itemCardResult, "item");
        assertThat(cardDto.count()).isZero();

        MvcResult cartResult = mockMvc.perform(get("/cart/items"))
                .andExpect(status().isOk())
                .andReturn();

        List<ItemDto> cartItems = extractModelAttribute(cartResult, "items");
        Long cartTotal = extractModelAttribute(cartResult, "total");
        assertThat(cartItems).isEmpty();
        assertThat(cartTotal).isZero();
    }

    private <T> T extractModelAttribute(MvcResult result, String name) {
        return (T) Objects.requireNonNull(result.getModelAndView()).getModel().get(name);
    }
}

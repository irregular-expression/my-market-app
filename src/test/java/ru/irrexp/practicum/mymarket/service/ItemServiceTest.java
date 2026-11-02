package ru.irrexp.practicum.mymarket.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.irrexp.practicum.mymarket.dto.ItemDto;
import ru.irrexp.practicum.mymarket.dto.ItemsPageResponse;
import ru.irrexp.practicum.mymarket.entity.CartItem;
import ru.irrexp.practicum.mymarket.entity.Item;
import ru.irrexp.practicum.mymarket.enums.ActionType;
import ru.irrexp.practicum.mymarket.enums.SortType;
import ru.irrexp.practicum.mymarket.repository.CartItemRepository;
import ru.irrexp.practicum.mymarket.repository.ItemRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class ItemServiceTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @BeforeEach
    void setUp() {
        cartItemRepository.deleteAll();
        itemRepository.deleteAll();
    }

    @Test
    void getItems_returnsPagedAndSortedResults() {
        for (int i = 0; i < 10; i++) {
            Item item = new Item();
            item.setTitle("Phone " + i);
            item.setDescription("Desc " + i);
            item.setImgPath("/static/img" + i + ".png");
            item.setPrice(1000 + i * 100);
            itemRepository.save(item);
        }
        itemRepository.flush();

        ItemsPageResponse response = itemService.getItems("Phone", SortType.PRICE, 1, 5);
        assertThat(response.items()).hasSize(2);
        assertThat(response.items().get(0)).hasSize(3);
        assertThat(response.paging().pageSize()).isEqualTo(5);
        assertThat(response.paging().pageNumber()).isEqualTo(1);
        assertThat(response.search()).isEqualTo("Phone");
        assertThat(response.sort()).isEqualTo(SortType.PRICE);
    }

    @Test
    void getItems_groupsIntoRowsOfThreeWithPlaceholders() {
        for (int i = 0; i < 4; i++) {
            Item item = new Item();
            item.setTitle("Group Test " + i);
            item.setPrice(200L);
            itemRepository.save(item);
        }

        ItemsPageResponse response = itemService.getItems(null, SortType.NO, 1, 10);
        List<List<ItemDto>> rows = response.items();
        assertThat(rows).hasSize(2);
        assertThat(rows.get(0)).hasSize(3);
        assertThat(rows.get(1)).hasSize(3);
        assertThat(rows.get(1).get(0).id()).isGreaterThanOrEqualTo(0);
        assertThat(rows.get(1).get(1).id()).isEqualTo(-1L);
        assertThat(rows.get(1).get(2).id()).isEqualTo(-1L);
    }

    @Test
    void getItem_returnsDtoForExistingItem() {
        Item item = new Item();
        item.setTitle("Single Item");
        item.setDescription("Only one");
        item.setImgPath("/static/single.png");
        item.setPrice(450L);
        item = itemRepository.save(item);

        ItemDto dto = itemService.getItem(item.getId());
        assertThat(dto.id()).isEqualTo(item.getId());
        assertThat(dto.title()).isEqualTo("Single Item");
        assertThat(dto.price()).isEqualTo(450L);
    }

    @Test
    void getItem_throwsWhenNotFound() {
        assertThatThrownBy(() -> itemService.getItem(9999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Товар не найден");
    }

    @Test
    void updateCartFromItemPage_createsCartItemIfMissingAndUpdatesCount() {
        Item item = new Item();
        item.setTitle("Single Item");
        item.setPrice(600L);
        item = itemRepository.save(item);

        ItemDto dto = itemService.updateCartFromItemPage(item.getId(), ActionType.PLUS);
        assertThat(dto.count()).isEqualTo(1);
        assertThat(dto.price()).isEqualTo(600L);

        dto = itemService.updateCartFromItemPage(item.getId(), ActionType.PLUS);
        assertThat(dto.count()).isEqualTo(2);
    }

    @Test
    void updateCartFromItemPage_decreasesCountAndHandlesMinusEdgeCase() {
        Item item = new Item();
        item.setTitle("Single Item");
        item.setPrice(800L);
        item = itemRepository.save(item);

        itemService.updateCartFromItemPage(item.getId(), ActionType.PLUS);
        itemService.updateCartFromItemPage(item.getId(), ActionType.PLUS);

        ItemDto dto = itemService.updateCartFromItemPage(item.getId(), ActionType.MINUS);
        assertThat(dto.count()).isEqualTo(1);

        dto = itemService.updateCartFromItemPage(item.getId(), ActionType.MINUS);
        assertThat(dto.count()).isEqualTo(0);
    }

    @Test
    void updateCart_delegatesToUpdateCartFromItemPage() {
        Item item = new Item();
        item.setTitle("Delegate Test");
        item.setPrice(350L);
        item = itemRepository.save(item);

        itemService.updateCart(item.getId(), "test", SortType.ALPHA, 1, 10, ActionType.PLUS);

        CartItem cartItem = cartItemRepository.findByItemId(item.getId()).orElseThrow();
        assertThat(cartItem.getCount()).isEqualTo(1);
    }
}


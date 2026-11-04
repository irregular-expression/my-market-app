package ru.irrexp.practicum.mymarket.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import ru.irrexp.practicum.mymarket.entity.Item;
import ru.irrexp.practicum.mymarket.repository.ItemRepository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = "mymarket.catalog.file=nonexistent.csv")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class CatalogLoaderServiceTest {

    @Autowired
    private CatalogLoaderService catalogLoaderService;

    @Autowired
    private ItemRepository itemRepository;

    private Path tempCatalogFile;

    @BeforeEach
    void setUp() throws IOException {
        tempCatalogFile = Files.createTempFile("catalog", ".csv");
        itemRepository.deleteAll();
    }

    @AfterEach
    void tearDown() throws IOException {
        if (Files.exists(tempCatalogFile)) {
            Files.delete(tempCatalogFile);
        }
    }

    @Test
    void loadCatalog_whenFileExistsAndValid_thenItemsAreSaved() throws Exception {
        String content = """
                Смартфон Pro X;Флагманский смартфон с камерой 200 МП и экраном 120 Гц.;/static/images/smartphone_pro_x.png;89900
                Ноутбук Ultra;Тонкий ноутбук с OLED-экраном и долгим временем работы;/static/images/laptop_ultra.png;129900
                """;
        Files.writeString(tempCatalogFile, content, StandardCharsets.UTF_8);

        catalogLoaderService.setCatalogFilePath(tempCatalogFile.toString());
        catalogLoaderService.loadCatalog();

        List<Item> items = itemRepository.findAll();
        assertThat(items).hasSize(2);

        Item phone = items.getFirst();
        assertThat(phone.getTitle()).isEqualTo("Смартфон Pro X");
        assertThat(phone.getPrice()).isEqualTo(89900L);
        assertThat(phone.getImgPath()).isEqualTo("/static/images/smartphone_pro_x.png");

        Item laptop = items.get(1);
        assertThat(laptop.getTitle()).isEqualTo("Ноутбук Ultra");
        assertThat(laptop.getPrice()).isEqualTo(129900L);
    }

    @Test
    void loadCatalog_whenFileDoesNotExist_thenNothingHappens() {
        catalogLoaderService.loadCatalog();

        assertThat(itemRepository.findAll()).isEmpty();
    }

    @Test
    void loadCatalog_withInvalidLines_thenOnlyValidItemsAreSaved() throws Exception {
        String content = """
                Смартфон Pro X;Флагманский смартфон с камерой 200 МП.;/static/images/smartphone_pro_x.png;89900
                ;Пустая строка, title отсутствует;;
                Плохой формат;Только три поля;/static/bad.png
                Ещё один товар;Описание товара;/static/another.png;-500
                Товар с отрицательной ценой;Описание;/static/negative.png;-100
                Последний товар;Описание;/static/last.png;999
                """;
        Files.writeString(tempCatalogFile, content, StandardCharsets.UTF_8);

        catalogLoaderService.setCatalogFilePath(tempCatalogFile.toString());
        catalogLoaderService.loadCatalog();

        List<Item> items = itemRepository.findAll();
        // Ожидаем только полностью валидные строки: 1-я и последняя
        assertThat(items).hasSize(2);

        Item first = items.getFirst();
        assertThat(first.getTitle()).isEqualTo("Смартфон Pro X");
        assertThat(first.getPrice()).isEqualTo(89900L);

        Item last = items.get(1);
        assertThat(last.getTitle()).isEqualTo("Последний товар");
        assertThat(last.getPrice()).isEqualTo(999L);
    }

    @Test
    @Transactional
    void loadCatalog_clearsTableBeforeInserting() throws Exception {
        // Сначала положим в БД какие-то данные
        Item existing = new Item();
        existing.setTitle("Old Item");
        existing.setDescription("Old");
        existing.setImgPath("/static/old.png");
        existing.setPrice(100L);
        itemRepository.save(existing);
        assertThat(itemRepository.findAll()).hasSize(1);

        String content = """
                Новый товар;Описание нового товара;/static/new.png;500
                """;
        Files.writeString(tempCatalogFile, content, StandardCharsets.UTF_8);
        catalogLoaderService.setCatalogFilePath(tempCatalogFile.toString());
        catalogLoaderService.loadCatalog();

        List<Item> items = itemRepository.findAll();
        assertThat(items).hasSize(1); // Старый товар удалён, остался только новый
        assertThat(items.getFirst().getTitle()).isEqualTo("Новый товар");
    }
}


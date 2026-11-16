package ru.irrexp.practicum.mymarket.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import ru.irrexp.practicum.mymarket.entity.Item;
import ru.irrexp.practicum.mymarket.repository.ItemRepository;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class CatalogLoaderServiceTest {

    @Autowired
    private CatalogLoaderService catalogLoaderService;

    @Autowired
    private ItemRepository itemRepository;

    @TempDir
    Path tempDir;

    @BeforeEach
    void cleanUp() {
        itemRepository.deleteAll().block();
    }

    private void seedCatalog() {
        Item existing1 = new Item();
        existing1.setTitle("Старый товар 1");
        existing1.setDescription("Описание");
        existing1.setImgPath("/img/old1.png");
        existing1.setPrice(1L);
        Item existing2 = new Item();
        existing2.setTitle("Старый товар 2");
        existing2.setDescription("Описание");
        existing2.setImgPath("/img/old2.png");
        existing2.setPrice(2L);
        itemRepository.saveAll(List.of(existing1, existing2)).collectList().block();
    }

    private Path writeFile(String content) throws Exception {
        Path file = tempDir.resolve("catalog_" + System.nanoTime() + ".csv");
        Files.writeString(file, content, StandardCharsets.UTF_8);
        return file;
    }

    private List<String> storedTitles() {
        return itemRepository.findAll().collectList().block().stream()
                .map(Item::getTitle)
                .toList();
    }

    @Test
    @DisplayName("Пустой файл — каталог не перезаписывается")
    void loadCatalog_emptyFile_keepsExistingData() throws Exception {
        seedCatalog();
        catalogLoaderService.setCatalogFilePath(writeFile("").toString());

        catalogLoaderService.loadCatalog();

        assertThat(itemRepository.count().block()).isEqualTo(2L);
        assertThat(storedTitles()).containsExactlyInAnyOrder("Старый товар 1", "Старый товар 2");
    }

    @Test
    @DisplayName("Файл только из невалидных строк — каталог не перезаписывается")
    void loadCatalog_invalidFile_keepsExistingData() throws Exception {
        seedCatalog();
        String invalid = """
                только-одна-колонка
                title;desc;img;not_a_number
                title;desc;img;-5
                ;desc;img;10
                title;desc;;10
                """;
        catalogLoaderService.setCatalogFilePath(writeFile(invalid).toString());

        catalogLoaderService.loadCatalog();

        assertThat(itemRepository.count().block()).isEqualTo(2L);
        assertThat(storedTitles()).containsExactlyInAnyOrder("Старый товар 1", "Старый товар 2");
    }

    @Test
    @DisplayName("Файл не существует — каталог не перезаписывается")
    void loadCatalog_missingFile_keepsExistingData() {
        seedCatalog();
        catalogLoaderService.setCatalogFilePath(tempDir.resolve("no_such_file.csv").toString());

        catalogLoaderService.loadCatalog();

        assertThat(itemRepository.count().block()).isEqualTo(2L);
    }

    @Test
    @DisplayName("Валидный файл со смешанными строками — каталог заменяется только валидными товарами")
    void loadCatalog_validFile_replacesCatalog() throws Exception {
        seedCatalog();
        String valid = """
                Молоко;Просто молоко;/img/milk.png;89
                Хлеб;Чёрный хлеб;/img/bread.png;45

                не-число;desc;/img/x.png;abc
                негативная;desc;/img/y.png;-1
                """;
        catalogLoaderService.setCatalogFilePath(writeFile(valid).toString());

        catalogLoaderService.loadCatalog();

        List<String> titles = storedTitles();
        assertThat(titles).containsExactlyInAnyOrder("Молоко", "Хлеб");
    }
}


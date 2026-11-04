package ru.irrexp.practicum.mymarket.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.irrexp.practicum.mymarket.entity.Item;
import ru.irrexp.practicum.mymarket.repository.ItemRepository;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CatalogLoaderService {

    private final ItemRepository itemRepository;

    @Setter
    @Value("${mymarket.catalog.file}")
    private String catalogFilePath;

    @PostConstruct
    @Transactional
    public void loadCatalog() {
        File catalogFile = new File(catalogFilePath);

        if (!catalogFile.exists() || !catalogFile.isFile()) {
            return;
        }

        List<Item> items = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(catalogFile), StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                parseLine(line).ifPresent(items::add);
            }
        } catch (IOException e) {
            return;
        }

        itemRepository.deleteAll();
        itemRepository.saveAll(items);
    }

    private Optional<Item> parseLine(String line) {
        if (line == null || line.isBlank()) {
            return Optional.empty();
        }

        String content = line.trim();
        String[] parts = content.split(";", -1);
        if (parts.length != 4) {
            return Optional.empty();
        }

        String title = parts[0].trim();
        String description = parts[1].trim();
        String imgPath = parts[2].trim();
        String priceRaw = parts[3].trim();

        if (title.isEmpty() || imgPath.isEmpty() || priceRaw.isEmpty()) {
            return Optional.empty();
        }

        long price;
        try {
            price = Long.parseLong(priceRaw);
        } catch (NumberFormatException e) {
            return Optional.empty();
        }

        if (price < 0) {
            return Optional.empty();
        }

        Item item = new Item();
        item.setTitle(title);
        item.setDescription(description);
        item.setImgPath(imgPath);
        item.setPrice(price);

        return Optional.of(item);
    }
}


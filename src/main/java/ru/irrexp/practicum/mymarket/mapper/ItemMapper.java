package ru.irrexp.practicum.mymarket.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.irrexp.practicum.mymarket.dto.ItemDto;
import ru.irrexp.practicum.mymarket.entity.CartItem;
import ru.irrexp.practicum.mymarket.entity.Item;

@Mapper(componentModel = "spring")
public interface ItemMapper {

    @Mapping(target = "count", ignore = true)
    ItemDto toDto(Item item);

    @Mapping(target = "id", source = "item.id")
    ItemDto mapCartItemToItemDto(CartItem cartItem, Item item);
}


package ru.irrexp.practicum.mymarket.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Named;
import ru.irrexp.practicum.mymarket.dto.ItemDto;
import ru.irrexp.practicum.mymarket.entity.CartItem;
import ru.irrexp.practicum.mymarket.entity.Item;

import java.util.Optional;

import static java.util.Objects.isNull;


@Mapper(componentModel = "spring")
public interface ItemMapper {

    ItemDto toDto(Item item);

    @Named("mapCartItemToItemDto")
    default ItemDto mapCartItemToItemDto(CartItem cartItem) {
        return Optional.ofNullable(cartItem)
                .filter(ci -> !isNull(ci.getItem()))
                .map(ci -> new ItemDto(
                        ci.getItem().getId(),
                        ci.getItem().getTitle(),
                        ci.getItem().getDescription(),
                        ci.getItem().getImgPath(),
                        ci.getItem().getPrice(),
                        ci.getCount()
                ))
                .orElse(null);


    }
}


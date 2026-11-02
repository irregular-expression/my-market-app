package ru.irrexp.practicum.mymarket.mapper;


import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Named;
import ru.irrexp.practicum.mymarket.dto.CartResponse;
import ru.irrexp.practicum.mymarket.dto.ItemDto;
import ru.irrexp.practicum.mymarket.entity.CartItem;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Mapper(componentModel = "spring", uses = ItemMapper.class)
public interface CartMapper {

    default CartResponse toCartResponse(List<CartItem> cartItems) {
        return new CartResponse(Optional.ofNullable(cartItems)
                                        .map(this::toItemDtoList)
                                        .orElse(Collections.emptyList()),
                                Optional.ofNullable(cartItems)
                                        .map(this::totalSum)
                                        .orElse(0L));
    }

    @Named("toItemDtoList")
    @IterableMapping(qualifiedByName = "mapCartItemToItemDto")
    List<ItemDto> toItemDtoList(List<CartItem> cartItems);

    @Named("totalSum")
    default long totalSum(List<CartItem> cartItems) {
        return cartItems == null ?
                0L :
                cartItems.stream()
                        .map(cartItem -> cartItem.getItem().getPrice() * cartItem.getCount())
                        .reduce(0L, Long::sum);
    }


}

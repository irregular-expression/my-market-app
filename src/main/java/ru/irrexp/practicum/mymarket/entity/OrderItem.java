package ru.irrexp.practicum.mymarket.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table("order_items")
public class OrderItem {

    @Id
    private Long id;

    private Long orderId;
    private Long itemId;

    private long price; // цена на момент заказа
    private int count;
}

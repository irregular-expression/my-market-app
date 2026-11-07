BEGIN;

-- Таблица товаров (items)
CREATE TABLE items (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    img_path VARCHAR(512),
    price BIGINT NOT NULL
);

-- Индекс для поиска по названию и описанию (для эндпоинта /items с параметром search)
CREATE INDEX idx_items_title_description ON items USING btree (title, description);


-- Таблица позиций в корзине (cart_items)
CREATE TABLE cart_items (
    id BIGSERIAL PRIMARY KEY,
    item_id BIGINT NOT NULL,
    count INT NOT NULL,
    CONSTRAINT fk_cart_items_item FOREIGN KEY (item_id) REFERENCES items(id) ON DELETE CASCADE
);

-- Уникальный индекс: один товар — одна позиция в корзине (чтобы не было дублей)
CREATE UNIQUE INDEX idx_cart_items_item ON cart_items (item_id);


-- Таблица заказов (orders)
CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    total_sum BIGINT NOT NULL
);


-- Таблица позиций заказа (order_items)
CREATE TABLE order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    item_id BIGINT,
    price BIGINT NOT NULL,      -- цена на момент заказа (снимок)
    count INT NOT NULL,
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_order_items_item FOREIGN KEY (item_id) REFERENCES items(id) ON DELETE SET NULL
);

-- Индекс по order_id для быстрого получения состава заказа
CREATE INDEX idx_order_items_order ON order_items (order_id);

COMMIT;
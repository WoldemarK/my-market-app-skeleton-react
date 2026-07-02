CREATE TABLE items
(
    id          BIGSERIAL PRIMARY KEY,
    title       VARCHAR(255)   NOT NULL,
    description VARCHAR(5000),
    img_path    VARCHAR(255),
    price       DECIMAL(10, 2) NOT NULL
);

CREATE TABLE orders
(
    id         BIGSERIAL PRIMARY KEY,
    total_sum  DECIMAL(12, 2) NOT NULL,
    order_date TIMESTAMP      NOT NULL
);

CREATE TABLE order_items
(
    id       BIGSERIAL PRIMARY KEY,
    item_id  BIGINT,
    title    VARCHAR(255),
    price    DECIMAL(12, 2) NOT NULL,
    count    INT,
    order_id BIGINT         NOT NULL
);


ALTER TABLE order_items
    ADD CONSTRAINT fk_order_items_order
        FOREIGN KEY (order_id)
            REFERENCES orders (id)
            ON DELETE CASCADE;

ALTER TABLE order_items
    ADD CONSTRAINT fk_order_items_item
        FOREIGN KEY (item_id)
            REFERENCES items (id)
            ON DELETE SET NULL;

CREATE INDEX idx_items_title ON items (title);

CREATE INDEX idx_order_items_order_id ON order_items (order_id);

CREATE INDEX idx_order_items_item_id ON order_items (item_id);

CREATE INDEX idx_orders_order_date ON orders (order_date);
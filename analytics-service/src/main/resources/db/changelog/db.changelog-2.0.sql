--liquibase formatted sql

--changeset sromanov:1
CREATE TABLE transaction_event (
    id UUID,
    user_id UUID,
    offer_id Int64,
    status String,
    card_type Nullable(String),
    created_at DateTime,
    confirmed_at Nullable(DateTime),
    cancelled_at Nullable(DateTime),
    card_number String,
    amount Decimal(19, 4),

    INDEX idx_tx_card_ts (card_number, confirmed_at) TYPE bloom_filter GRANULARITY 1
)
ENGINE = MergeTree()
ORDER BY (created_at, id);
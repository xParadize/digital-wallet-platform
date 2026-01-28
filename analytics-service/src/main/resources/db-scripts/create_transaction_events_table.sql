CREATE TABLE transaction_events
(
    id UUID,
    user_id UUID,
    offer_id UInt64,
    status LowCardinality(String),
    card_number String,
    card_type LowCardinality(String),
    amount Decimal(18, 2),
    transaction_type LowCardinality(String),
    created_at DateTime64(3),
    confirmed_at Nullable(DateTime64(3)),
    cancelled_at Nullable(DateTime64(3)),
    created_date Date MATERIALIZED toDate(created_at)
)
    ENGINE = MergeTree
        PARTITION BY toYYYYMM(created_date)
        ORDER BY (user_id, created_at)
        SETTINGS index_granularity = 8192;
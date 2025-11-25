--liquibase formatted sql

--changeset sromanov:1 runInTransaction:false
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_tx_card_ts ON transaction_ (card_number, confirmed_at DESC);
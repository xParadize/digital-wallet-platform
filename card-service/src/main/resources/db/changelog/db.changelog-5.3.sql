--liquibase formatted sql

--changeset sromanov:1
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_card_user_id ON card_ (user_id);

--changeset sromanov:2
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_card_details_number ON card_details (number);

--changeset sromanov:3
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_card_user_id_balance ON card_ (user_id, balance);

--changeset sromanov:4
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_card_details_card_id_exp_date ON card_details (card_id, expiration_date);

--changeset sromanov:5
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_metadata_card_id_issuer ON card_metadata (card_id, issuer);

--changeset sromanov:6
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_card_limit_card_id_amount ON limit_ (card_id, limit_amount);
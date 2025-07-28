--liquibase formatted sql

--changeset sromanov:1
CREATE TABLE IF NOT EXISTS transaction_ (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    offer_id VARCHAR(255) NOT NULL,
    "status" VARCHAR(50) NOT NULL,
    category VARCHAR(50) NOT NULL,
    amount NUMERIC(19, 4) NOT NULL,
    currency VARCHAR(10) NOT NULL,
    card_type VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    confirmed_at TIMESTAMP WITHOUT TIME ZONE,
    cancelled_at TIMESTAMP WITHOUT TIME ZONE
);
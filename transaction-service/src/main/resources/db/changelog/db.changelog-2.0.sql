--liquibase formatted sql

--changeset sromanov:1
CREATE TABLE IF NOT EXISTS payment_offer (
    id VARCHAR(255) PRIMARY KEY,
    amount NUMERIC(19, 4) NOT NULL,
    currency VARCHAR(10) NOT NULL,
    category VARCHAR(50) NOT NULL,
    vendor VARCHAR(255),
    latitude FLOAT,
    longitude FLOAT,
    suggested_at TIMESTAMP WITHOUT TIME ZONE NOT NULL
);

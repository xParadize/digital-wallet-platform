--liquibase formatted sql

--changeset sromanov:1
CREATE TABLE IF NOT EXISTS cards (
    id BIGINT PRIMARY KEY,
    user_id UUID NOT NULL,
    number VARCHAR(32) NOT NULL,
    expiration_date VARCHAR(5) NOT NULL,
    cvv CHAR(3) NOT NULL,
    money NUMERIC(19, 4) NOT NULL
);
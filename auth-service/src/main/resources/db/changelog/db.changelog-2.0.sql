--liquibase formatted sql

--changeset sromanov:1
CREATE TABLE IF NOT EXISTS user_prototype
(
    id UUID PRIMARY KEY,
    phone VARCHAR NOT NULL UNIQUE,
    email VARCHAR(254) NOT NULL UNIQUE,
    password VARCHAR NOT NULL
);
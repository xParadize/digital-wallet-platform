--liquibase formatted sql

--changeset sromanov:1
CREATE TABLE IF NOT EXISTS user_
(
    id UUID PRIMARY KEY,
    name VARCHAR NOT NULL CHECK (char_length(name) >= 2),
    lastname VARCHAR NOT NULL CHECK (char_length(lastname) >= 2),
    patronymic VARCHAR NOT NULL,
    birthDate DATE NOT NULL,
    phone VARCHAR NOT NULL UNIQUE,
    email VARCHAR(254) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT now()
);
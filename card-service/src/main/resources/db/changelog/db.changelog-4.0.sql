--liquibase formatted sql

--changeset sromanov:1
CREATE TABLE limit_ (
    id SERIAL PRIMARY KEY,
    perTransactionLimit NUMERIC
);

--changeset sromanov:2
ALTER TABLE card_ ADD limit_id BIGINT UNIQUE REFERENCES limit_(id);
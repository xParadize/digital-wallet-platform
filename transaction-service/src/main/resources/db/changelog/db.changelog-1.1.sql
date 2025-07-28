--liquibase formatted sql

--changeset sromanov:1
ALTER TABLE transaction_ ALTER COLUMN card_type DROP NOT NULL;
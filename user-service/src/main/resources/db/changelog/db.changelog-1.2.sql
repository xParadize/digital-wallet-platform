--liquibase formatted sql

--changeset sromanov:1
ALTER TABLE user_ ALTER COLUMN birth_date TYPE VARCHAR(10) USING birth_date::VARCHAR(10);
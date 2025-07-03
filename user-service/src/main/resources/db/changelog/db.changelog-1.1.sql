--liquibase formatted sql

--changeset sromanov:1
ALTER TABLE user_ RENAME COLUMN birthdate TO birth_date;
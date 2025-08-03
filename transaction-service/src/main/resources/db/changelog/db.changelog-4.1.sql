--liquibase formatted sql

--changeset sromanov:1
ALTER TABLE transaction_ ADD COLUMN amount NUMERIC(19, 4) NOT NULL;

--liquibase formatted sql

--changeset sromanov:1
ALTER TABLE transaction_ ADD COLUMN fee NUMERIC(19, 4) NOT NULL;
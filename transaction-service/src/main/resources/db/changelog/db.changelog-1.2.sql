--liquibase formatted sql

--changeset sromanov:1
ALTER TABLE transaction_ ADD card_number VARCHAR NOT NULL;
--liquibase formatted sql

--changeset sromanov:1
ALTER TABLE card_ ADD card_scheme VARCHAR(50) NOT NULL;

--changeset sromanov:2
ALTER TABLE card_ ADD card_issuer VARCHAR(255) NOT NULL;

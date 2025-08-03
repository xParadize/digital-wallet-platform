--liquibase formatted sql

--changeset sromanov:1
ALTER TABLE transaction_ DROP COLUMN category;

--changeset sromanov:2
ALTER TABLE transaction_ DROP COLUMN amount;

--changeset sromanov:3
ALTER TABLE transaction_ DROP COLUMN currency;

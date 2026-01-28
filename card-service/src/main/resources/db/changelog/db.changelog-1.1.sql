--liquibase formatted sql

--changeset sromanov:1
ALTER TABLE cards RENAME TO card_;
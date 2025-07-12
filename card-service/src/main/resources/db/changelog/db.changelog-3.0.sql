--liquibase formatted sql

--changeset sromanov:1
ALTER TABLE card_ ADD frozen BOOLEAN DEFAULT false NOT NULL;

--changeset sromanov:2
ALTER TABLE card_ ADD blocked BOOLEAN DEFAULT false NOT NULL;
--liquibase formatted sql

--changeset sromanov:1
ALTER TABLE limit_ ADD limit_enabled BOOLEAN DEFAULT FALSE;
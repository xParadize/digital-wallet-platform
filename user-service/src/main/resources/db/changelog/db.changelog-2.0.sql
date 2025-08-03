--liquibase formatted sql

--changeset sromanov:1
ALTER TABLE user_ ALTER COLUMN created_at TYPE TIMESTAMP WITH TIME ZONE USING created_at::TIMESTAMP WITH TIME ZONE;
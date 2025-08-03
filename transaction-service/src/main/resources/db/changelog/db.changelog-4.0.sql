--liquibase formatted sql

--changeset sromanov:1
ALTER TABLE transaction_ ALTER COLUMN created_at TYPE TIMESTAMP WITH TIME ZONE USING created_at::TIMESTAMP WITH TIME ZONE;

--changeset sromanov:2
ALTER TABLE transaction_ ALTER COLUMN confirmed_at TYPE TIMESTAMP WITH TIME ZONE USING confirmed_at::TIMESTAMP WITH TIME ZONE;

--changeset sromanov:3
ALTER TABLE transaction_ ALTER COLUMN cancelled_at TYPE TIMESTAMP WITH TIME ZONE USING cancelled_at::TIMESTAMP WITH TIME ZONE;

--changeset sromanov:4
ALTER TABLE payment_offer ALTER COLUMN suggested_at TYPE TIMESTAMP WITH TIME ZONE USING suggested_at::TIMESTAMP WITH TIME ZONE;

--changeset sromanov:5
ALTER TABLE payment_offer ALTER COLUMN completed_at TYPE TIMESTAMP WITH TIME ZONE USING completed_at::TIMESTAMP WITH TIME ZONE;

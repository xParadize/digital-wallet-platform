--liquibase formatted sql

--changeset sromanov:1
ALTER TABLE limit_ ALTER COLUMN pertransactionlimit TYPE NUMERIC(19, 4) USING pertransactionlimit::NUMERIC(19, 4);

--changeset sromanov:2
ALTER TABLE limit_ RENAME COLUMN pertransactionlimit TO per_transaction_limit;
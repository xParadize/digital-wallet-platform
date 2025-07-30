--liquibase formatted sql

--changeset sromanov:1
ALTER TABLE payment_offer ADD completed_at TIMESTAMP WITHOUT TIME ZONE;

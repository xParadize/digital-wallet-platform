--liquibase formatted sql

--changeset sromanov:1
ALTER TABLE transaction_ ADD CONSTRAINT fk_transaction_offer FOREIGN KEY (offer_id) REFERENCES payment_offer(id) ON DELETE RESTRICT ON UPDATE CASCADE;

--changeset sromanov:2
ALTER TABLE transaction_ ALTER COLUMN offer_id SET NOT NULL;

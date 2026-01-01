--liquibase formatted sql

--changeset sromanov:1
CREATE TABLE outbox (
    id SERIAL PRIMARY KEY,
    event_type TEXT NOT NULL,
    payload TEXT NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW()
);

--changeset sromanov:2
CREATE INDEX idx_outbox_created_at ON outbox(created_at);
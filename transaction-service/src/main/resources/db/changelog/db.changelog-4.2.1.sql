--liquibase formatted sql

--changeset sromanov:1 runInTransaction:false
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_tx_status_ts ON transaction_ (status, created_at DESC);
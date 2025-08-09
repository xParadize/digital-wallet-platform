--liquibase formatted sql

--changeset sromanov:1
CREATE TABLE IF NOT EXISTS expense_analysis_report (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    card_number VARCHAR(32) NOT NULL,
    period_from DATE NOT NULL,
    period_to DATE NOT NULL,
    report TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    requested_at TIMESTAMP WITH TIME ZONE
);
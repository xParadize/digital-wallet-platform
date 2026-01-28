CREATE TABLE IF NOT EXISTS expense_analysis_report (
    id UUID,
    card_number String,
    period_from Date,
    period_to Date,
    report String,
    created_at DateTime64(3)
)
    ENGINE = MergeTree()
        ORDER BY (card_number, period_from, period_to);
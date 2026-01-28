CREATE TABLE IF NOT EXISTS report_view_log (
    report_id UUID,
    viewed_at DateTime64(3)
)
ENGINE = MergeTree()
ORDER BY (report_id, viewed_at);
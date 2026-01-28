package com.wallet.analyticsservice.repository;

import com.wallet.analyticsservice.entity.ExpenseAnalysisReport;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExpenseAnalysisReportRepository {
    private final JdbcTemplate jdbcTemplate;

    public ExpenseAnalysisReport saveExpenseReport(ExpenseAnalysisReport report) {
        if (report.getId() == null) {
            report.setId(UUID.randomUUID());
        }

        String sql = """
                INSERT INTO expense_analysis_report (
                    id, card_number, period_from, period_to, report, created_at
                ) VALUES (?, ?, ?, ?, ?, ?)
                """;

        jdbcTemplate.update(sql,
                report.getId(),
                report.getCardNumber(),
                report.getPeriodFrom(),
                report.getPeriodTo(),
                report.getReport(),
                report.getCreatedAt() != null ? Timestamp.from(report.getCreatedAt()) : null
        );

        return report;
    }

    public void saveReportView(UUID reportId, Instant viewedAt) {
        String sql = "INSERT INTO report_view_log (report_id, viewed_at) VALUES (?, ?)";

        jdbcTemplate.update(sql,
                reportId,
                Timestamp.from(viewedAt)
        );
    }

    public Optional<ExpenseAnalysisReport> findById(UUID id) {
        String sql = """
                SELECT id, card_number, period_from, period_to, report, created_at
                FROM expense_analysis_report
                WHERE id = ?
                """;
        List<ExpenseAnalysisReport> results = jdbcTemplate.query(sql, reportRowMapper, id);
        return results.stream().findFirst();
    }

    public Optional<ExpenseAnalysisReport> findFirstByCardNumberAndPeriod(String cardNumber, LocalDate periodFrom, LocalDate periodTo) {
        String sql = """
                SELECT id, card_number, period_from, period_to, report, created_at
                FROM expense_analysis_report
                WHERE card_number = ? 
                  AND period_from = ? 
                  AND period_to = ?
                LIMIT 1
                """;
        List<ExpenseAnalysisReport> results = jdbcTemplate.query(sql, reportRowMapper, cardNumber, periodFrom, periodTo);
        return results.stream().findFirst();
    }

    private final RowMapper<ExpenseAnalysisReport> reportRowMapper = (rs, rowNum) -> {
        ExpenseAnalysisReport report = new ExpenseAnalysisReport();
        report.setId(rs.getObject("id", UUID.class));
        report.setCardNumber(rs.getString("card_number"));
        report.setPeriodFrom(rs.getObject("period_from", LocalDate.class));
        report.setPeriodTo(rs.getObject("period_to", LocalDate.class));
        report.setReport(rs.getString("report"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            report.setCreatedAt(createdAt.toInstant());
        }
        return report;
    };
}
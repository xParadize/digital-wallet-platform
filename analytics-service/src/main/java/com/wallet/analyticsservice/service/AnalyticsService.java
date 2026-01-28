package com.wallet.analyticsservice.service;

import com.wallet.analyticsservice.dto.CategorySpendingReportRequest;
import com.wallet.analyticsservice.entity.ExpenseAnalysisReport;
import com.wallet.analyticsservice.exception.ExpenseAnalysisReportNotFoundException;
import com.wallet.analyticsservice.repository.ExpenseAnalysisReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AnalyticsService {
    private final AiService aiService;
    private final ExpenseAnalysisReportRepository expenseAnalysisReportRepository;

    public UUID saveAnalytics(CategorySpendingReportRequest request) throws IOException, InterruptedException {
        ExpenseAnalysisReport newReport = expenseAnalysisReportRepository.saveExpenseReport(analysisBuilder(request));
        return newReport.getId();
    }

    private ExpenseAnalysisReport analysisBuilder(CategorySpendingReportRequest request) throws IOException, InterruptedException {
        return ExpenseAnalysisReport.builder()
                .cardNumber(request.cardNumber())
                .periodFrom(request.from())
                .periodTo(request.to())
                .report(aiService.generateSpendingAnalysis(request.categorySpendingList()))
                .createdAt(Instant.now())
                .build();
    }

    public ExpenseAnalysisReport findExpenseReportByCardAndPeriod(String cardNumber, LocalDate from, LocalDate to) {
        return expenseAnalysisReportRepository.findFirstByCardNumberAndPeriod(cardNumber, from, to)
                .orElse(null);
    }

    public String getExpenseReportById(UUID reportId) {
        ExpenseAnalysisReport report = expenseAnalysisReportRepository.findById(reportId)
                .orElseThrow(() -> new ExpenseAnalysisReportNotFoundException("Report " + reportId + " not found"));
        expenseAnalysisReportRepository.saveReportView(report.getId(), Instant.now());
        return report.getReport();
    }
}

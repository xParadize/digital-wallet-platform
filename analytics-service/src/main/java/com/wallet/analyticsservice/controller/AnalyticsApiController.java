package com.wallet.analyticsservice.controller;

import com.wallet.analyticsservice.dto.CategorySpendingReportRequest;
import com.wallet.analyticsservice.entity.ExpenseAnalysisReport;
import com.wallet.analyticsservice.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsApiController {
    private final AnalyticsService analyticsService;

    @PostMapping("/")
    public ResponseEntity<String> analyzeExpenses(@RequestBody CategorySpendingReportRequest request) throws IOException, InterruptedException {
        ExpenseAnalysisReport report = analyticsService.findExpenseReportByCardAndPeriod(request.cardNumber(), request.from(), request.to());
        if (report == null) {
            UUID reportId = analyticsService.saveAnalytics(request);
            return new ResponseEntity<>(analyticsService.getLinkToReport(reportId), HttpStatus.OK);
        }
        return new ResponseEntity<>(analyticsService.getLinkToReport(report.getId()), HttpStatus.OK);
    }
}

package com.wallet.analyticsservice.controller;

import com.wallet.analyticsservice.dto.CategorySpendingReportRequest;
import com.wallet.analyticsservice.entity.ExpenseAnalysisReport;
import com.wallet.analyticsservice.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsApiController {
    private final AnalyticsService analyticsService;

    @PostMapping("/report")
    public ResponseEntity<String> saveExpenseReport(@RequestBody CategorySpendingReportRequest request) throws IOException, InterruptedException {
        ExpenseAnalysisReport report = analyticsService.findExpenseReportByCardAndPeriod(request.cardNumber(), request.from(), request.to());
        String reportLink = "http://localhost:8991/api/v1/analytics/report/";
        if (report == null) {
            UUID reportId = analyticsService.saveAnalytics(request);
            return new ResponseEntity<>(reportLink + reportId, HttpStatus.OK);
        }
        return new ResponseEntity<>(reportLink + report.getId(), HttpStatus.OK);
    }

    @GetMapping("/report/{report_id}")
    public ResponseEntity<String> getExpenseReport(@PathVariable("report_id") String reportId) {
        return new ResponseEntity<>(analyticsService.getExpenseReportById(UUID.fromString(reportId)), HttpStatus.OK);
    }
}

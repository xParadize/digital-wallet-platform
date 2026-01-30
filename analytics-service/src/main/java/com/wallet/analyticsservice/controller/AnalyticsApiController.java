package com.wallet.analyticsservice.controller;

import com.wallet.analyticsservice.dto.CategorySpendingReportRequest;
import com.wallet.analyticsservice.entity.ExpenseAnalysisReport;
import com.wallet.analyticsservice.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics API", description = "Expense analysis and report generation endpoints")
public class AnalyticsApiController {
    private final AnalyticsService analyticsService;

    @Operation(summary = "Create or get expense report", description = "Finds existing expense report by card and period, or creates a new one. Returns report URL.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Report link (existing or newly created)", content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request body")
    })
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

    @Operation(summary = "Get expense report by ID", description = "Returns the expense analysis report content for the given report ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Report content (e.g. JSON or text)"),
            @ApiResponse(responseCode = "404", description = "Report not found")
    })
    @GetMapping("/report/{report_id}")
    public ResponseEntity<String> getExpenseReport(
            @Parameter(description = "Report UUID") @PathVariable("report_id") String reportId) {
        return new ResponseEntity<>(analyticsService.getExpenseReportById(UUID.fromString(reportId)), HttpStatus.OK);
    }
}

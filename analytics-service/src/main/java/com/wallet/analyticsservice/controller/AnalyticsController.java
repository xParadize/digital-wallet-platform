package com.wallet.analyticsservice.controller;

import com.wallet.analyticsservice.dto.ApiResponse;
import com.wallet.analyticsservice.entity.ExpenseAnalysisReport;
import com.wallet.analyticsservice.exception.IncorrectSearchPath;
import com.wallet.analyticsservice.exception.InvalidAuthorizationException;
import com.wallet.analyticsservice.service.AnalyticsService;
import com.wallet.analyticsservice.service.JwtService;
import com.wallet.analyticsservice.util.AnalyticsRequestsValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/analytics")
public class AnalyticsController {
    private final AnalyticsService analyticsService;
    private final AnalyticsRequestsValidator analyticsRequestsValidator;
    private final JwtService jwtService;

    @RequestMapping(value = "/**")
    public ResponseEntity<ApiResponse> handleNotFound() {
        throw new IncorrectSearchPath();
    }

    @GetMapping("/{report_id}")
    public String getExpenseReport(@PathVariable("report_id") @NotNull UUID reportId,
                                   @RequestHeader("Authorization") String authorizationHeader) {
        String jwt = extractJwtFromHeader(authorizationHeader);
        UUID userId = UUID.fromString(jwtService.extractUserIdFromJwt(jwt));

        ExpenseAnalysisReport expenseAnalysis = analyticsService.getExpenseReportById(reportId);

        analyticsRequestsValidator.validateUserCardAccess(expenseAnalysis.getCardNumber(), userId);

        return expenseAnalysis.getReport();
    }

    private String extractJwtFromHeader(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new InvalidAuthorizationException("Invalid authorization header");
        }
        return authorizationHeader.substring(7);
    }
}

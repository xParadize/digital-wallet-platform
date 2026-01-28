package com.wallet.transactionservice.feign;

import com.wallet.transactionservice.dto.CategorySpendingReportRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "${digital-wallet-platform.services.analytics-service.uri}")
public interface AnalyticsFeignClient {

    @PostMapping("/api/v1/analytics/report")
    ResponseEntity<String> analyzeExpenses(@RequestBody CategorySpendingReportRequest request);
}
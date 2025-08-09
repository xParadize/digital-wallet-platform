package com.wallet.transactionservice.feign;

import com.wallet.transactionservice.dto.CategorySpendingReportRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "analytics-client",
        url = "http://localhost:8087")
public interface AnalyticsFeignClient {

    @PostMapping("/api/v1/analytics/")
    ResponseEntity<String> analyzeExpenses(@RequestBody CategorySpendingReportRequest request);
}
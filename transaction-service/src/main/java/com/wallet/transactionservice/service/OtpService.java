package com.wallet.transactionservice.service;

import com.wallet.transactionservice.dto.OtpInitRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OtpService {
    private final RestClient restClient = RestClient.create();
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    public void initiateOtp(UUID userId, String offerId) {
        OtpInitRequest otpRequest = new OtpInitRequest(userId, offerId);

        try {
            restClient.post()
                    .uri("http://localhost:8100/api/v1/otp/generate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(otpRequest)
                    .retrieve()
                    .toBodilessEntity();
            LOGGER.info("OTP generated via Go service for user {} and offer {}", userId, offerId);
        } catch (Exception ex) {
            LOGGER.error("Failed to generate OTP via Go: {}", ex.getMessage(), ex);
            throw new IllegalStateException("Unable to generate OTP via Go", ex);
        }
    }
}

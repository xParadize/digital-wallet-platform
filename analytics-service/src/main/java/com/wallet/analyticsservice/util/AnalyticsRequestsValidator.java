package com.wallet.analyticsservice.util;

import com.wallet.analyticsservice.dto.CardDetailsDto;
import com.wallet.analyticsservice.exception.CardAccessDeniedException;
import com.wallet.analyticsservice.feign.CardFeignClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AnalyticsRequestsValidator {
    private final CardFeignClient cardFeignClient;

    public void validateUserCardAccess(String cardNumber, UUID userId) {
        CardDetailsDto cardDetailsDto = cardFeignClient.getLinkedCard(cardNumber, userId).getBody();
        validateCardOwnership(cardDetailsDto, userId);
    }

    private void validateCardOwnership(CardDetailsDto cardDetails, UUID userId) {
        if (!cardDetails.getHolder().id().equals(userId)) {
            throw new CardAccessDeniedException("Not your card");
        }
    }
}

package com.wallet.cardservice.util;

import com.wallet.cardservice.dto.CardInfoResponse;
import com.wallet.cardservice.dto.CardMeta;
import com.wallet.cardservice.exception.ExternalApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@Component
@RequiredArgsConstructor
public class CardInfoCollector {
    private static final String API_URL = "https://data.handyapi.com/bin/{cardNumber}";
    private final RestClient restClient = RestClient.create();

    public CardMeta getCardMeta(String cardNumber) {
        try {
            CardInfoResponse info = getCardInfoResponse(cardNumber);
            return new CardMeta(info.Scheme(), info.Issuer());
        } catch (RestClientException e) {
            throw new ExternalApiException("Error when receiving card metadata: " + e.getMessage(), e);
        }
    }

    private CardInfoResponse getCardInfoResponse(String cardNumber) {
        return restClient.get()
                .uri(API_URL, cardNumber)
                .accept(APPLICATION_JSON)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        (req, res) -> {
                            res.getBody();
                            String body = res.getBody().toString();
                            throw new ExternalApiException("Error from API: " + res.getStatusCode() + " — " + body);
                        })
                .body(CardInfoResponse.class);
    }
}

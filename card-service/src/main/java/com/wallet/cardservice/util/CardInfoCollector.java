package com.wallet.cardservice.util;

import com.wallet.cardservice.dto.CardMetaResponse;
import com.wallet.cardservice.dto.CardMeta;
import com.wallet.cardservice.exception.ExternalApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@Component
@RequiredArgsConstructor
public class CardInfoCollector {
    private static final String API_URL = "https://data.handyapi.com/bin/{card_number}";
    private final static RestClient restClient = RestClient.create();

    public CardMeta getCardMeta(String cardNumber) {
        try {
            CardMetaResponse response = fetchCardInfo(cardNumber);
            return new CardMeta(response.Scheme(), response.Issuer());
        } catch (RestClientException e) {
            throw new ExternalApiException("Error when receiving card metadata: " + e.getMessage(), e);
        }
    }

    private CardMetaResponse fetchCardInfo(String cardNumber) {
        return restClient.get()
                .uri(API_URL, cardNumber)
                .accept(APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    throw new ExternalApiException("API error: " + res.getStatusCode() + " - " +
                                    new String(res.getBody().readAllBytes())
                    );
                })
                .body(CardMetaResponse.class);
    }
}

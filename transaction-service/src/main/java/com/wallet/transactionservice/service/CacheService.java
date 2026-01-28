package com.wallet.transactionservice.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.wallet.transactionservice.dto.PaymentOffer;
import com.wallet.transactionservice.entity.PaymentOfferEntity;
import com.wallet.transactionservice.exception.PaymentOfferNotFoundException;
import com.wallet.transactionservice.util.InstantDeserializer;
import com.wallet.transactionservice.util.InstantSerializer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class CacheService {
    private final RedisTemplate<String, String> redisTemplate;
    private static final String PAYMENT_OFFER_KEY = "offer:";
    private static final long PAYMENT_OFFER_TTL_MS = 300_000;

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Instant.class, new InstantDeserializer())
            .registerTypeAdapter(Instant.class, new InstantSerializer())
            .create();

    public PaymentOffer getPaymentOfferById(String offerId) {
        try {
            String key = PAYMENT_OFFER_KEY + offerId;
            String json = redisTemplate.opsForValue().get(key);

            if (json == null) {
                log.warn("No offer found in Redis for id: {}", offerId);
                throw new PaymentOfferNotFoundException("No offer found for id: " + offerId);
            }

            log.debug("Successfully retrieved offer {} from Redis", offerId);
            return GSON.fromJson(json, PaymentOffer.class);

        } catch (PaymentOfferNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Redis error while getting offer with id {}", offerId, e);
            throw new RuntimeException("Redis error", e);
        }
    }

    public void removeOffer(String offerId) {
        try {
            String key = PAYMENT_OFFER_KEY + offerId;
            redisTemplate.delete(key);
        } catch (Exception e) {
            throw new RuntimeException("Redis error", e);
        }
    }

    public void returnOffer(PaymentOffer offer) {
        try {
            String key = PAYMENT_OFFER_KEY + offer.id();
            String json = GSON.toJson(offer);
            redisTemplate.opsForValue().set(key, json, PAYMENT_OFFER_TTL_MS, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            throw new RuntimeException("Redis error", e);
        }
    }
}

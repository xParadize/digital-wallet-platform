package com.wallet.transactionservice.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.wallet.transactionservice.exception.PaymentOfferNotFoundException;
import com.wallet.transactionservice.dto.PaymentOffer;
import com.wallet.transactionservice.util.LocalDateTimeDeserializer;
import com.wallet.transactionservice.util.LocalDateTimeSerializer;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.UnifiedJedis;
import redis.clients.jedis.exceptions.JedisException;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CacheService {
    @Value("${REDIS_HOST}")
    private String redisHost;

    @Value("${REDIS_PORT}")
    private int redisPort;

    private final String PAYMENT_OFFER_KEY = "offer:";
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeDeserializer())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeSerializer())
            .create();
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    public PaymentOffer getPaymentOfferById(String offerId) {
        try (UnifiedJedis jedis = new UnifiedJedis(String.format("http://%s:%s", redisHost, redisPort))) {
            String key = PAYMENT_OFFER_KEY + offerId;
            String json = jedis.get(key);

            if (json == null) {
                throw new PaymentOfferNotFoundException("No offer found for id: " + offerId);
            }

            return GSON.fromJson(json, PaymentOffer.class);
        } catch (JedisException e) {
            LOGGER.error("Redis error while getting offer with id {}", offerId, e);
            throw new JedisException("Redis error", e);
        }
    }

    @Transactional
    public void removeOffer(String offerId) {
        try (UnifiedJedis jedis = new UnifiedJedis(String.format("http://%s:%s", redisHost, redisPort))) {
            String key = PAYMENT_OFFER_KEY + offerId;
            jedis.del(key);
        } catch (JedisException e) {
            LOGGER.error("Redis error while removing offer with id {}", offerId, e);
            throw new JedisException("Redis error", e);
        }
    }
}

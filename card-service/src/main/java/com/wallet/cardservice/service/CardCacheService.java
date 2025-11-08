package com.wallet.cardservice.service;

import com.wallet.cardservice.dto.CardInfoDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CardCacheService {
    private final RedisTemplate<String, CardInfoDto> cardCacheTemplate;
    private static final String CARD_CACHE_KEY = "card:";
    private static final long CARD_TTL = 1_209_600_000; // 2 weeks

    public CardInfoDto getCardById(Long cardId, UUID userId) {
        return cardCacheTemplate.opsForValue().get(CARD_CACHE_KEY + cardId + ":user:" + userId);
    }

    public void saveCard(Long cardId, UUID userId, CardInfoDto cardInfo) {
        cardCacheTemplate.opsForValue().set(
                CARD_CACHE_KEY + cardId + ":user:" + userId,
                cardInfo,
                Duration.ofMillis(CARD_TTL));
    }
}

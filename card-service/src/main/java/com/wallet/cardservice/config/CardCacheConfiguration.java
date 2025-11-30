package com.wallet.cardservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wallet.cardservice.dto.CachedCardInfo;
import com.wallet.cardservice.dto.CardInfoDto;
import com.wallet.cardservice.dto.Holder;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
@EnableCaching
public class CardCacheConfiguration {
    private static final long CARD_TTL = 1_209_600_000; // 2 weeks
    private static final long HOLDER_TTL = 86_400_000; // 1 day

    @Bean
    public RedisCacheManager cardCacheManager(RedisConnectionFactory connectionFactory, ObjectMapper objectMapper) {
        var jsonSerializer1 = new Jackson2JsonRedisSerializer<>(objectMapper, CachedCardInfo.class);
        var jsonSerializer2 = new Jackson2JsonRedisSerializer<>(objectMapper, Holder.class);

        RedisCacheConfiguration cardConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMillis(CARD_TTL))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer1))
                .computePrefixWith(cacheName -> cacheName + ":");

        RedisCacheConfiguration cardByNumberConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMillis(CARD_TTL))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer1))
                .computePrefixWith(cacheName -> cacheName + ":");

        RedisCacheConfiguration holderConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMillis(HOLDER_TTL))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer2))
                .computePrefixWith(cacheName -> cacheName + ":");

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(cardConfig)
                .withCacheConfiguration("card", cardConfig)
                .withCacheConfiguration("cardByNumber", cardByNumberConfig)
                .withCacheConfiguration("holder", holderConfig)
                .transactionAware()
                .build();
    }
}

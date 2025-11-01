package com.wallet.notificationservice.config;

import io.micrometer.observation.ObservationPredicate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.observation.ClientRequestObservationContext;

/**
 * Конфигурация трассировки (tracing) для Micrometer.
 * <p>
 * В этом классе отключается сбор трассировки (span'ов) для:<p>
 *  - внутренних health-check запросов<p>
 *  - пингов Eureka (запросов, содержащих "/eureka/**")
 * <p>
 * Это нужно, чтобы не засорять метрики и логи служебными запросами,
 * не влияющими на бизнес-логику и производительность сервиса.
 */
@Configuration
public class TracingConfiguration {
    private final static String SKIP_TRACING_EUREKA_ENDPOINT = "/eureka/";

    @Value("${management.tracing.include-eureka-healthcheck}")
    private boolean includeEurekaHealthCheck;

    /**
     * Фильтр для исключения из трассировки служебных HTTP-запросов:
     * Micrometer не будет собирать observation'ы (span'ы) для запросов к Eureka и health checks.
     */
    @Bean
    public ObservationPredicate skipEurekaAndHealthChecks() {
        return (name, context) -> {
            if (includeEurekaHealthCheck) {
                return true;
            }

            if (context instanceof ClientRequestObservationContext clientContext) {
                String uri = clientContext.getCarrier().getURI().toString();
                return !uri.contains(SKIP_TRACING_EUREKA_ENDPOINT);
            }
            return true;
        };
    }
}
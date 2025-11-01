package com.wallet.walletservice.config;

import feign.RequestInterceptor;
import io.micrometer.tracing.Tracer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignTracingConfiguration {

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${server.port}")
    private String serverPort;

    @Bean
    public RequestInterceptor tracingRequestInterceptor(Tracer tracer) {
        return requestTemplate -> {
            if (tracer.currentSpan() != null) {
                tracer.currentSpan().tag("client.service", applicationName);
                tracer.currentSpan().tag("client.port", serverPort);

                String traceId = tracer.currentSpan().context().traceId();
                String spanId = tracer.currentSpan().context().spanId();

                requestTemplate.header("X-B3-TraceId", traceId);
                requestTemplate.header("X-B3-SpanId", spanId);
                requestTemplate.header("X-B3-Sampled", "1");

                requestTemplate.header("X-Service-Instance", applicationName + "-" + serverPort);

                if (tracer.currentSpan().context().parentId() != null) {
                    requestTemplate.header("X-B3-ParentSpanId",
                            tracer.currentSpan().context().parentId());
                }
            }
        };
    }
}
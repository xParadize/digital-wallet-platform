package com.wallet.walletservice.config;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Фильтр для добавления тегов трассировки к входящим HTTP-запросам.
 * <p>
 * При получении входящего HTTP-запроса создаётся server span, к которому этот фильтр
 * добавляет следующие теги:
 * <ul>
 *   <li><b>client.service</b> - имя текущего сервиса (из {@code spring.application.name})</li>
 *   <li><b>client.port</b> - порт текущего сервиса (из {@code server.port})</li>
 * </ul>
 * <p>
 * Это позволяет идентифицировать сервис-получатель в distributed tracing системах (например, Zipkin),
 * дополняя информацию о client span'ах, которые добавляются при исходящих запросах
 * через {@link FeignTracingConfiguration}.
 * <p>
 * <b>Примечание:</b> Без этого фильтра server span'ы не содержат информации о порте и имени
 * сервиса-получателя, что затрудняет анализ трассировки.
 */
@Component
public class ServerSpanTaggingFilter implements Filter {
    private final Tracer tracer;
    private final String applicationName;
    private final String serverPort;

    public ServerSpanTaggingFilter(Tracer tracer,
                                   @Value("${spring.application.name}") String applicationName,
                                   @Value("${server.port}") String serverPort) {
        this.tracer = tracer;
        this.applicationName = applicationName;
        this.serverPort = serverPort;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        Span currentSpan = tracer.currentSpan();
        if (currentSpan != null) {
            currentSpan.tag("client.service", applicationName);
            currentSpan.tag("client.port", serverPort);
        }

        chain.doFilter(request, response);
    }
}
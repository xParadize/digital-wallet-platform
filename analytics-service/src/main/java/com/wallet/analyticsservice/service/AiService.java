package com.wallet.analyticsservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wallet.analyticsservice.dto.CategorySpending;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

@Service
public class AiService {
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${spring.ai.openai.api-key}")
    private String API_KEY;

    @Value("${spring.ai.openai.base-url}")
    private String BASE_URL;

    public String generateSpendingAnalysis(List<CategorySpending> categorySpendingList) throws IOException, InterruptedException {
        String jsonData = mapper.writeValueAsString(categorySpendingList);

        String prompt = String.format("""
            Ты — эксперт по финансовой аналитике.
            Тебе будет передан JSON с категориями расходов и суммами.
            Проанализируй данные и выведи короткий связный текст (4–6 предложений) без таблиц и списков.
            
            Требования к тексту:
            1. Не пиши категории расходов так, как они указаны в исходных данных (не используй CAPS и английские названия). 
               Вместо этого используй естественные описания на русском языке, например:
               "продукты", "подписки", "налоги", "одежда", "благотворительность", "доставка", "улучшение жилья" и т.п.
            2. Укажи, какая категория имеет наибольшие расходы и какая — наименьшие.
            3. Примерно оцени долю лидирующих категорий от общей суммы.
            4. Опиши общую картину распределения трат (равномерно или сосредоточено).
            5. Дай 1–2 конкретных и полезных совета по оптимизации бюджета.
            
            Пиши простым, понятным языком, без технических терминов и форматирования.
            Тон — дружелюбный, но профессиональный.
            
            Пример входных данных:
            [
                { "category": "CHARITY", "spending": 658303.8502 },
                { "category": "SUBSCRIPTIONS", "spending": 627996.0112 },
                { "category": "TAXES", "spending": 202495.8338 }
            ]
            
            Входные данные: %s.
            """, jsonData);


        Map<String, Object> requestMap = Map.of(
                "model", "gpt-4o",
                "messages", List.of(
                        Map.of("role", "system", "content", "You are a helpful AI Assistant"),
                        Map.of("role", "user", "content", prompt)
                )
        );

        String requestBody = mapper.writeValueAsString(requestMap);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/chat/completions"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + API_KEY)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        JsonNode root = mapper.readTree(response.body());
        return root.path("choices").get(0).path("message").path("content").asText();
    }
}

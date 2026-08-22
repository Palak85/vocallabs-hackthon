package com.hackathon.backend.service.nlp;

import com.hackathon.backend.dto.NlpAnalysisResponse;
import com.hackathon.backend.service.NlpService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service("httpNlpService")
@ConditionalOnProperty(name = "nlp.mock.enabled", havingValue = "false")
public class HttpNlpService implements NlpService {

    private final RestClient restClient;
    private final String nlpServiceUrl;

    public HttpNlpService(
            @Value("${nlp.service.url:http://localhost:8000/api/nlp/analyze}") String nlpServiceUrl) {
        this.nlpServiceUrl = nlpServiceUrl;
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(5));
        requestFactory.setReadTimeout(Duration.ofSeconds(15));
        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }

    @Override
    public NlpAnalysisResponse analyze(String text) {
        return analyze(text, null, null);
    }

    @Override
    public NlpAnalysisResponse analyze(String text, String conversationId, String messageId) {
        try {
            log.info("Sending text to external NLP Service at: {} (convId={}, msgId={})", nlpServiceUrl, conversationId, messageId);
            Map<String, Object> payload = new HashMap<>();
            payload.put("text", text != null ? text : "");
            if (conversationId != null && !conversationId.isBlank()) {
                payload.put("conversation_id", conversationId);
            }
            if (messageId != null && !messageId.isBlank()) {
                payload.put("message_id", messageId);
            }

            return restClient.post()
                    .uri(nlpServiceUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .body(NlpAnalysisResponse.class);
        } catch (Exception e) {
            log.error("Failed to invoke external NLP Service at {}: {}", nlpServiceUrl, e.getMessage());
            throw new RuntimeException("External NLP Service failure: " + e.getMessage(), e);
        }
    }
}

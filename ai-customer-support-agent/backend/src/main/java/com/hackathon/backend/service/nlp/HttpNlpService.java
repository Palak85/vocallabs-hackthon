package com.hackathon.backend.service.nlp;

import com.hackathon.backend.dto.NlpAnalysisResponse;
import com.hackathon.backend.service.NlpService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Slf4j
@Service("httpNlpService")
@ConditionalOnProperty(name = "nlp.mock.enabled", havingValue = "false")
public class HttpNlpService implements NlpService {

    private final RestClient restClient;
    private final String nlpServiceUrl;

    public HttpNlpService(
            RestClient.Builder restClientBuilder,
            @Value("${nlp.service.url:http://localhost:8000/api/nlp/analyze}") String nlpServiceUrl) {
        this.nlpServiceUrl = nlpServiceUrl;
        this.restClient = restClientBuilder.build();
    }

    @Override
    public NlpAnalysisResponse analyze(String text) {
        try {
            log.info("Sending text to external NLP Service at: {}", nlpServiceUrl);
            return restClient.post()
                    .uri(nlpServiceUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("text", text != null ? text : ""))
                    .retrieve()
                    .body(NlpAnalysisResponse.class);
        } catch (Exception e) {
            log.error("Failed to invoke external NLP Service at {}: {}", nlpServiceUrl, e.getMessage());
            throw new RuntimeException("External NLP Service failure: " + e.getMessage(), e);
        }
    }
}

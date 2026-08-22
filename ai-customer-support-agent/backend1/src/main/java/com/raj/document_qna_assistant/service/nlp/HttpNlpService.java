package com.raj.document_qna_assistant.service.nlp;

import com.raj.document_qna_assistant.dto.NlpAnalysisRequest;
import com.raj.document_qna_assistant.dto.NlpAnalysisResponse;
import com.raj.document_qna_assistant.service.NlpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service("httpNlpService")
public class HttpNlpService implements NlpService {

    private static final Logger log = LoggerFactory.getLogger(HttpNlpService.class);

    private final RestClient restClient;
    private final MockNlpService fallbackMockService;
    private final String serviceUrl;

    public HttpNlpService(
            @Value("${nlp.service.url:http://localhost:8000/api/nlp/analyze}") String serviceUrl,
            MockNlpService fallbackMockService) {
        this.serviceUrl = serviceUrl;
        this.fallbackMockService = fallbackMockService;
        this.restClient = RestClient.builder()
                .baseUrl(serviceUrl)
                .build();
    }

    @Override
    public NlpAnalysisResponse analyze(NlpAnalysisRequest request) {
        log.info("Dispatching NLP analysis request to HTTP endpoint: {}", serviceUrl);
        try {
            NlpAnalysisResponse response = restClient.post()
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(NlpAnalysisResponse.class);

            if (response != null && response.success()) {
                return response;
            }
            log.warn("HTTP NLP response returned unsuccessful, falling back to mock");
            return fallbackMockService.analyze(request);
        } catch (Exception e) {
            log.error("Failed to connect to external NLP service at {}: {}. Falling back to deterministic mock.",
                    serviceUrl, e.getMessage());
            return fallbackMockService.analyze(request);
        }
    }
}

package com.raj.document_qna_assistant.service.nlp;

import com.raj.document_qna_assistant.dto.NlpAnalysisRequest;
import com.raj.document_qna_assistant.dto.NlpAnalysisResponse;
import com.raj.document_qna_assistant.service.NlpService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
public class DelegatingNlpService implements NlpService {

    private final NlpService mockNlpService;
    private final NlpService httpNlpService;

    @Value("${nlp.mock.enabled:true}")
    private boolean mockEnabled;

    public DelegatingNlpService(
            @Qualifier("mockNlpService") NlpService mockNlpService,
            @Qualifier("httpNlpService") NlpService httpNlpService) {
        this.mockNlpService = mockNlpService;
        this.httpNlpService = httpNlpService;
    }

    @Override
    public NlpAnalysisResponse analyze(NlpAnalysisRequest request) {
        if (mockEnabled) {
            return mockNlpService.analyze(request);
        }
        return httpNlpService.analyze(request);
    }
}

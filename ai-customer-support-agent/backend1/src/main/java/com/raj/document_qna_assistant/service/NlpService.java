package com.raj.document_qna_assistant.service;

import com.raj.document_qna_assistant.dto.NlpAnalysisRequest;
import com.raj.document_qna_assistant.dto.NlpAnalysisResponse;

public interface NlpService {
    NlpAnalysisResponse analyze(NlpAnalysisRequest request);
}

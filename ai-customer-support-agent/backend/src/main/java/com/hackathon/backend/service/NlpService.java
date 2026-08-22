package com.hackathon.backend.service;

import com.hackathon.backend.dto.NlpAnalysisResponse;

public interface NlpService {

    NlpAnalysisResponse analyze(String text);

    NlpAnalysisResponse analyze(String text, String conversationId, String messageId);
}

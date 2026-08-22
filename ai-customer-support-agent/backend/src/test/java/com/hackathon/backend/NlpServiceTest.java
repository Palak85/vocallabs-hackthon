package com.hackathon.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackathon.backend.dto.NlpAnalysisResponse;
import com.hackathon.backend.service.nlp.MockNlpService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NlpServiceTest {

    private MockNlpService mockNlpService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockNlpService = new MockNlpService();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testMockNlpRefundRequest() {
        String query = "Can I get a refund for my subscription?";
        NlpAnalysisResponse response = mockNlpService.analyze(query);

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertNotNull(response.getNlp());
        assertEquals("billing", response.getNlp().getDomain().getLabel());
        assertEquals("refund_request", response.getNlp().getIntent().getLabel());
        assertEquals("negative", response.getNlp().getSentiment().getLabel());
        assertTrue(response.getNlp().getFrustration().getScore() >= 50);
    }

    @Test
    void testMockNlpInsuranceClaimDelay() {
        String query = "Why is my claim CLM-99882 delayed?";
        NlpAnalysisResponse response = mockNlpService.analyze(query);

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("insurance", response.getNlp().getDomain().getLabel());
        assertEquals("claim_delay", response.getNlp().getIntent().getLabel());
        assertEquals("CLM-99882", response.getNlp().getFirstEntityValue("claim_number"));
        assertEquals("high", response.getNlp().getUrgency().getLevel());
    }

    @Test
    void testMockNlpGeneralInquiry() {
        String query = "What are your business hours?";
        NlpAnalysisResponse response = mockNlpService.analyze(query);

        assertNotNull(response);
        assertEquals("general", response.getNlp().getDomain().getLabel());
        assertEquals("general_inquiry", response.getNlp().getIntent().getLabel());
        assertEquals("neutral", response.getNlp().getSentiment().getLabel());
    }

    @Test
    void testDtoSerializationAndDeserialization() throws Exception {
        String query = "I am having issues with my billing";
        NlpAnalysisResponse original = mockNlpService.analyze(query);

        String json = objectMapper.writeValueAsString(original);
        assertNotNull(json);
        assertTrue(json.contains("conversation_id"));
        assertTrue(json.contains("frustration_trend"));

        NlpAnalysisResponse deserialized = objectMapper.readValue(json, NlpAnalysisResponse.class);
        assertNotNull(deserialized);
        assertEquals(original.isSuccess(), deserialized.isSuccess());
        assertEquals(original.getNlp().getIntent().getLabel(), deserialized.getNlp().getIntent().getLabel());
    }
}

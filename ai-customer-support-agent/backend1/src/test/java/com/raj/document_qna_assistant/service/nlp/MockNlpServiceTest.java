package com.raj.document_qna_assistant.service.nlp;

import com.raj.document_qna_assistant.dto.NlpAnalysisRequest;
import com.raj.document_qna_assistant.dto.NlpAnalysisResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MockNlpServiceTest {

    private MockNlpService mockNlpService;

    @BeforeEach
    void setUp() {
        mockNlpService = new MockNlpService();
    }

    @Test
    void testBankingUpiFailedTransactionAnalysis() {
        NlpAnalysisRequest request = new NlpAnalysisRequest(
                "conv_001",
                "msg_001",
                "cust_123",
                "My UPI transaction failed but money was deducted. TXN12345"
        );

        NlpAnalysisResponse response = mockNlpService.analyze(request);

        assertNotNull(response);
        assertTrue(response.success());
        assertEquals("conv_001", response.conversationId());
        assertEquals("msg_001", response.messageId());

        assertNotNull(response.nlp());
        assertEquals("en", response.nlp().language().label());
        assertEquals("banking", response.nlp().domain().label());
        assertEquals("transaction_failed", response.nlp().intent().label());
        assertEquals("negative", response.nlp().sentiment().label());
        assertEquals("frustrated", response.nlp().emotion().label());
        assertEquals("high", response.nlp().frustration().level());
        assertEquals(72, response.nlp().frustration().score());
        assertEquals("medium", response.nlp().urgency().level());

        assertNotNull(response.nlp().entities());
        assertFalse(response.nlp().entities().isEmpty());
        assertEquals("transaction_id", response.nlp().entities().get(0).type());
        assertEquals("TXN12345", response.nlp().entities().get(0).value());

        assertNotNull(response.conversationAnalysis());
        assertEquals("increasing", response.conversationAnalysis().frustrationTrend());
    }

    @Test
    void testRefundRequestAnalysis() {
        NlpAnalysisRequest request = new NlpAnalysisRequest(
                "conv_002",
                "msg_002",
                "cust_456",
                "I want a refund for my order ORD9988 because it was damaged."
        );

        NlpAnalysisResponse response = mockNlpService.analyze(request);

        assertNotNull(response);
        assertEquals("e-commerce", response.nlp().domain().label());
        assertEquals("refund_request", response.nlp().intent().label());
        assertEquals("frustrated", response.nlp().emotion().label());
        assertEquals("medium", response.nlp().frustration().level());
        assertEquals("high", response.nlp().urgency().level());
    }

    @Test
    void testGeneralInquiryAnalysis() {
        NlpAnalysisRequest request = new NlpAnalysisRequest(
                "conv_003",
                "msg_003",
                "cust_789",
                "What are your business opening hours?"
        );

        NlpAnalysisResponse response = mockNlpService.analyze(request);

        assertNotNull(response);
        assertEquals("general_inquiry", response.nlp().domain().label());
        assertEquals("information_lookup", response.nlp().intent().label());
        assertEquals("neutral", response.nlp().sentiment().label());
        assertEquals("low", response.nlp().frustration().level());
    }
}

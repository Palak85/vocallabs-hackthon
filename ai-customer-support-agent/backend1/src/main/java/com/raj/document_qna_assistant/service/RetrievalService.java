package com.raj.document_qna_assistant.service;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RetrievalService {

    private final VectorStore vectorStore;

    public RetrievalService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public List<Document> retrieveChunks(String query, String tenantId, String category, int topK, double threshold) {
        FilterExpressionBuilder builder = new FilterExpressionBuilder();
        
        Filter.Expression filterExpression;
        if (category != null && !category.isBlank()) {
            filterExpression = builder.and(
                builder.eq("tenant_id", tenantId),
                builder.eq("category", category)
            ).build();
        } else {
            filterExpression = builder.eq("tenant_id", tenantId).build();
        }

        SearchRequest request = SearchRequest.builder()
                .query(query)
                .topK(topK)
                .similarityThreshold(threshold)
                .filterExpression(filterExpression)
                .build();

        return vectorStore.similaritySearch(request);
    }
}

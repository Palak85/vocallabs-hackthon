package com.raj.document_qna_assistant;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Flux;

import java.util.List;

@TestConfiguration
public class TestModelsConfig {

    @Bean
    @Primary
    public EmbeddingModel embeddingModel() {
        return new EmbeddingModel() {
            @Override
            public float[] embed(Document document) {
                return getVector(document.getText());
            }

            @Override
            public List<float[]> embed(List<String> texts) {
                return texts.stream().map(this::getVector).toList();
            }

            @Override
            public EmbeddingResponse call(EmbeddingRequest request) {
                List<Embedding> list = request.getInstructions().stream()
                        .map(text -> new Embedding(getVector(text), 0))
                        .toList();
                return new EmbeddingResponse(list);
            }

            private float[] getVector(String text) {
                float[] vector = new float[768];
                if (text == null || text.isBlank()) {
                    vector[0] = 1.0f;
                    return vector;
                }
                if (text.contains("Unrelated")) {
                    vector[1] = 1.0f;
                } else {
                    vector[0] = 1.0f;
                }
                return vector;
            }
        };
    }

    @Bean
    @Primary
    public ChatModel chatModel() {
        return new ChatModel() {
            @Override
            public ChatResponse call(Prompt prompt) {
                return new ChatResponse(List.of(new Generation(new org.springframework.ai.chat.messages.AssistantMessage("This is a factual grounded answer from the mock model."))));
            }

            @Override
            public Flux<ChatResponse> stream(Prompt prompt) {
                return Flux.just(
                        new ChatResponse(List.of(new Generation(new org.springframework.ai.chat.messages.AssistantMessage("Mock streamed answer text."))))
                );
            }
        };
    }
}

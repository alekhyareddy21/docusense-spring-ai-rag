package com.docusense.ai.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class AiConfig {

    private static final Logger log = LoggerFactory.getLogger(AiConfig.class);

    @Value("${spring.ai.openai.api-key:}")
    private String apiKey;

    @Value("${spring.ai.openai.base-url:https://api.openai.com}")
    private String baseUrl;

    @Bean
    @Primary
    public EmbeddingModel embeddingModel() {
        if (hasValidApiKey()) {
            try {
                log.info("Configuring live OpenAiEmbeddingModel with base URL: {}", baseUrl);
                OpenAiApi openAiApi = new OpenAiApi(baseUrl, apiKey);
                return new OpenAiEmbeddingModel(openAiApi);
            } catch (Exception e) {
                log.warn("Failed to initialize OpenAiEmbeddingModel: {}. Falling back to local model.", e.getMessage());
            }
        }
        log.info("Using local FallbackEmbeddingModel (offline RAG mode).");
        return new FallbackEmbeddingModel();
    }

    @Bean
    @ConditionalOnProperty(prefix = "spring.ai.openai", name = "api-key")
    public ChatModel chatModel() {
        if (hasValidApiKey()) {
            try {
                log.info("Configuring live OpenAiChatModel with base URL: {}", baseUrl);
                OpenAiApi openAiApi = new OpenAiApi(baseUrl, apiKey);
                return new OpenAiChatModel(openAiApi);
            } catch (Exception e) {
                log.warn("Failed to initialize OpenAiChatModel: {}", e.getMessage());
            }
        }
        return null;
    }

    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        log.info("Initializing SimpleVectorStore with EmbeddingModel: {}", embeddingModel.getClass().getSimpleName());
        return new SimpleVectorStore(embeddingModel);
    }

    private boolean hasValidApiKey() {
        return apiKey != null && !apiKey.isBlank() && !apiKey.startsWith("demo_") && !apiKey.equalsIgnoreCase("unconfigured");
    }
}

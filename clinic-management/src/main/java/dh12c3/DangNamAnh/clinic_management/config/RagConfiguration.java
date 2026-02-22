package dh12c3.DangNamAnh.clinic_management.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.model.googleai.GoogleAiEmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.qdrant.QdrantEmbeddingStore;
import dev.langchain4j.data.segment.TextSegment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class RagConfiguration {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${langchain4j.gemini.embedding-model}")
    private String embeddingModelName;

    @Value("${langchain4j.qdrant.host}")
    private String qdrantHost;

    @Value("${langchain4j.qdrant.port}")
    private int qdrantPort;

    @Value("${langchain4j.qdrant.collection-name}")
    private String collectionName;

    @Value("${langchain4j.gemini.model-name}")
    private String chatModelName;

    @Value("${langchain4j.gemini.temperature}")
    private Double temperature;

    @Bean
    public EmbeddingModel embeddingModel() {
        return GoogleAiEmbeddingModel.builder()
                .apiKey(geminiApiKey)
                .modelName(embeddingModelName)
                .build();
    }

    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        return QdrantEmbeddingStore.builder()
                .host(qdrantHost)
                .port(qdrantPort)
                .collectionName(collectionName)
                .build();
    }

    @Bean
    public ChatLanguageModel chatLanguageModel() {
        return GoogleAiGeminiChatModel.builder()
                .apiKey(geminiApiKey)
                .modelName(chatModelName)
                .temperature(temperature)
                .timeout(Duration.ofSeconds(300))
                .maxRetries(3)
                .build();
    }
}
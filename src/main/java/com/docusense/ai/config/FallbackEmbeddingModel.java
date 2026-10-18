package com.docusense.ai.config;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;

import java.util.ArrayList;
import java.util.List;

public class FallbackEmbeddingModel implements EmbeddingModel {

    private static final int DIMENSIONS = 128;

    @Override
    public EmbeddingResponse call(EmbeddingRequest request) {
        List<Embedding> embeddings = new ArrayList<>();
        int index = 0;
        for (String text : request.getInstructions()) {
            float[] vector = generatePseudoEmbedding(text);
            embeddings.add(new Embedding(vector, index++));
        }
        return new EmbeddingResponse(embeddings);
    }

    @Override
    public float[] embed(Document document) {
        return generatePseudoEmbedding(document.getContent());
    }

    @Override
    public float[] embed(String text) {
        return generatePseudoEmbedding(text);
    }

    /**
     * Deterministic pseudo-embedding based on character frequencies and hash codes,
     * normalized so cosine similarity works for keyword/phrase proximity.
     */
    private float[] generatePseudoEmbedding(String text) {
        float[] vector = new float[DIMENSIONS];
        if (text == null || text.isBlank()) {
            return vector;
        }

        String lower = text.toLowerCase();
        for (int i = 0; i < lower.length(); i++) {
            char c = lower.charAt(i);
            int idx = Math.abs((c * 31 + i) % DIMENSIONS);
            vector[idx] += 1.0f;
        }

        // L2 normalize
        float sumSq = 0.0f;
        for (float v : vector) {
            sumSq += v * v;
        }
        float norm = (float) Math.sqrt(sumSq);
        if (norm > 0) {
            for (int i = 0; i < DIMENSIONS; i++) {
                vector[i] /= norm;
            }
        }
        return vector;
    }
}

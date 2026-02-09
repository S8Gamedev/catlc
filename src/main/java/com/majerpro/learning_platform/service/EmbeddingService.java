package com.majerpro.learning_platform.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
public class EmbeddingService {

    private final WebClient webClient;
    private final Gson gson;

    // Using HuggingFace's free inference API
    private static final String EMBEDDING_API_URL =
            "https://api-inference.huggingface.co/pipeline/feature-extraction/sentence-transformers/all-MiniLM-L6-v2";

    private static final String API_KEY = ""; // We'll use public endpoint for now

    public EmbeddingService() {
        this.webClient = WebClient.builder()
                .baseUrl(EMBEDDING_API_URL)
                .build();
        this.gson = new Gson();
    }

    /**
     * Generate embedding for a text string
     * Returns a vector of floats representing the semantic meaning
     */
    public float[] generateEmbedding(String text) {
        try {
            // Call HuggingFace API
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("inputs", text);

            String response = webClient.post()
                    .bodyValue(requestBody.toString())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            // Parse response to float array
            JsonArray jsonArray = gson.fromJson(response, JsonArray.class);
            float[] embedding = new float[jsonArray.size()];

            for (int i = 0; i < jsonArray.size(); i++) {
                embedding[i] = jsonArray.get(i).getAsFloat();
            }

            return embedding;

        } catch (Exception e) {
            // Fallback: return dummy embedding for demo purposes
            System.err.println("Embedding API failed, using fallback: " + e.getMessage());
            return generateDummyEmbedding(text);
        }
    }

    /**
     * Calculate cosine similarity between two vectors
     */
    public double cosineSimilarity(float[] vec1, float[] vec2) {
        if (vec1.length != vec2.length) {
            throw new IllegalArgumentException("Vectors must have same dimension");
        }

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < vec1.length; i++) {
            dotProduct += vec1[i] * vec2[i];
            normA += vec1[i] * vec1[i];
            normB += vec2[i] * vec2[i];
        }

        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    /**
     * Convert float array to JSON string for storage
     */
    public String vectorToString(float[] vector) {
        return gson.toJson(vector);
    }

    /**
     * Convert JSON string back to float array
     */
    public float[] stringToVector(String vectorString) {
        return gson.fromJson(vectorString, float[].class);
    }

    /**
     * Fallback: Generate dummy embedding based on text hash
     * Used when API is unavailable
     */
    private float[] generateDummyEmbedding(String text) {
        int dimension = 384; // Standard dimension for all-MiniLM-L6-v2
        float[] embedding = new float[dimension];

        // Simple hash-based generation for demo
        int hash = text.hashCode();
        for (int i = 0; i < dimension; i++) {
            embedding[i] = (float) Math.sin(hash + i) * 0.1f;
        }

        return embedding;
    }
}

package com.majerpro.learning_platform.service.practice;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference; // NEW
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List; // NEW
import java.util.Map;  // NEW

@Service
public class Judge0Client {

    private final WebClient webClient;

    public Judge0Client(
            @Value("${judge0.base-url}") String baseUrl,
            @Value("${judge0.api-key:}") String apiKey
    ) {
        WebClient.Builder builder = WebClient.builder().baseUrl(baseUrl);

        // If you later use RapidAPI / paid Judge0, apiKey may be needed in headers
        if (apiKey != null && !apiKey.isBlank()) {
            builder.defaultHeader("X-Auth-Token", apiKey);
        }
        this.webClient = builder.build();
    }

    public SubmitResponse createSubmission(SubmitRequest request) {
        return webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/submissions")
                        .queryParam("base64_encoded", "false")
                        .queryParam("wait", "false")
                        .build())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(SubmitResponse.class)
                .block();
    }

    public SubmissionResult getSubmission(String token) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/submissions/{token}")
                        .queryParam("base64_encoded", "false")
                        .queryParam("fields", "status,stdout,stderr,compile_output,time,memory")
                        .build(token))
                .retrieve()
                .bodyToMono(SubmissionResult.class)
                .block();
    }

    public List<Map<String, Object>> getLanguages() {
        return webClient.get()
                .uri("/languages")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                .block();
    }

    // =========================
    // Batch APIs (multi test cases) // NEW
    // =========================

    public List<BatchSubmitResponseItem> createBatch(BatchSubmitRequest request) { // NEW
        return webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/submissions/batch")
                        .queryParam("base64_encoded", "false")
                        .build())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<BatchSubmitResponseItem>>() {})
                .block();
    }

    public BatchGetResponse getBatch(String tokensCsv) { // NEW
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/submissions/batch")
                        .queryParam("tokens", tokensCsv)
                        .queryParam("base64_encoded", "false")
                        .queryParam("fields", "token,status,stdout,stderr,compile_output,time,memory")
                        .build())
                .retrieve()
                .bodyToMono(BatchGetResponse.class)
                .block();
    }

    @Data
    public static class SubmitRequest {
        private Integer language_id;
        private String source_code;
        private String stdin;

        private String expected_output;  // NEW (Judge0 compares stdout vs expected_output)
        private Boolean enable_network;  // NEW (set false for deployed safety)
    }

    @Data
    public static class SubmitResponse {
        private String token;
    }

    @Data
    public static class SubmissionResult {
        private String token; // NEW (useful for batch mapping)
        private Status status;
        private String stdout;
        private String stderr;
        private String compile_output;
        private String time;
        private Integer memory;

        @Data
        public static class Status {
            private Integer id;
            private String description;
        }
    }

    // ===== Batch DTOs ===== // NEW
    @Data
    public static class BatchSubmitRequest { // NEW
        private List<SubmitRequest> submissions;
    }

    @Data
    public static class BatchSubmitResponseItem { // NEW
        private String token;
    }

    @Data
    public static class BatchGetResponse { // NEW
        private List<SubmissionResult> submissions;
    }
}

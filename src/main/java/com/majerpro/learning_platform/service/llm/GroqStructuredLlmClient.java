package com.majerpro.learning_platform.service.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.majerpro.learning_platform.config.GroqProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
public class GroqStructuredLlmClient implements StructuredLlmClient {

    private final WebClient groqWebClient;
    private final GroqProperties groqProperties;
    private final ObjectMapper objectMapper;

    public GroqStructuredLlmClient(WebClient groqWebClient,
                                   GroqProperties groqProperties,
                                   ObjectMapper objectMapper) {
        this.groqWebClient = groqWebClient;
        this.groqProperties = groqProperties;
        this.objectMapper = objectMapper;
    }

    @Override
    public String generateHierarchyJson(String skillName) {
        String prompt = """
                Return valid JSON only.
                No markdown.
                No explanation.

                Build a hierarchical learning tree for the skill: %s

                The response schema must be:
                {
                  "skill": "string",
                  "nodes": [
                    {
                      "title": "string",
                      "objective": "string",
                      "children": [
                        {
                          "title": "string",
                          "objective": "string",
                          "children": []
                        }
                      ]
                    }
                  ]
                }

                Rules:
                - Keep top-level nodes between 4 and 8
                - Each node should be educationally meaningful
                - Avoid duplicate titles
                - Children should represent important subtopics
                - Output must be strict JSON only
                """.formatted(skillName);

        return callGroq(prompt);
    }

    @Override
    public String summarizeNodeContent(String skillName, String nodeTitle, List<String> sourceChunks) {
        String joinedSources = String.join("\n\n---\n\n", sourceChunks);

        String prompt = """
                Return valid JSON only.
                No markdown.
                No explanation.

                You are given source text for a skill and one node in its concept hierarchy.

                Skill: %s
                Node: %s

                Source text:
                %s

                Produce JSON with this schema:
                {
                  "nodeTitle": "string",
                  "summary": "string",
                  "keyPoints": ["string"],
                  "example": "string"
                }

                Rules:
                - Use only the provided source text
                - Do not invent facts
                - Keep summary concise but useful
                - keyPoints should contain 3 to 6 items
                - Output strict JSON only
                """.formatted(skillName, nodeTitle, joinedSources);

        return callGroq(prompt);
    }

    private String callGroq(String prompt) {
        try {
            Map<String, Object> requestBody = Map.of(
                    "model", groqProperties.getModel(),
                    "temperature", groqProperties.getTemperature(),
                    "messages", List.of(
                            Map.of("role", "system", "content", "You are a structured JSON generator."),
                            Map.of("role", "user", "content", prompt)
                    ),
                    "response_format", Map.of("type", "json_object")
            );

            String response = groqWebClient.post()
                    .uri("/chat/completions")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode root = objectMapper.readTree(response);
            JsonNode contentNode = root.path("choices").get(0).path("message").path("content");

            if (contentNode.isMissingNode() || contentNode.isNull()) {
                throw new IllegalStateException("Groq response missing content");
            }

            return contentNode.asText();
        } catch (Exception e) {
            throw new RuntimeException("Failed to call Groq API", e);
        }
    }
}
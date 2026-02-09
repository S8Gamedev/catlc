package com.majerpro.learning_platform.service;

import com.majerpro.learning_platform.dto.ChatRequestDto;
import com.majerpro.learning_platform.dto.ChatResponseDto;
import com.majerpro.learning_platform.dto.SearchQueryDto;
import com.majerpro.learning_platform.dto.SearchResultDto;
import com.majerpro.learning_platform.model.*;
import com.majerpro.learning_platform.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.Gson;
import com.google.gson.JsonParser;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AIService {

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VectorStoreService vectorStoreService;

    @Autowired
    private KnowledgeStateRepository knowledgeStateRepository;

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.model}")
    private String model;

    @Value("${gemini.temperature}")
    private double temperature;

    @Value("${gemini.max.tokens}")
    private int maxTokens;

    private final WebClient webClient;
    private final Gson gson;

    public AIService() {
        this.webClient = WebClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com")
                .build();
        this.gson = new Gson();
    }

    @Transactional
    public ChatResponseDto chat(ChatRequestDto request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // DEBUG: check key length
        System.out.println("Gemini key length: " + (apiKey == null ? "null" : apiKey.length()));

        Conversation conversation;
        if (request.getConversationId() != null) {
            conversation = conversationRepository.findById(request.getConversationId())
                    .orElseThrow(() -> new RuntimeException("Conversation not found"));
        } else {
            conversation = new Conversation();
            conversation.setUser(user);
            conversation.setTitle(generateTitle(request.getMessage()));
            conversation = conversationRepository.save(conversation);
        }

        Message userMessage = new Message();
        userMessage.setConversation(conversation);
        userMessage.setRole("user");
        userMessage.setContent(request.getMessage());
        messageRepository.save(userMessage);

        List<SearchResultDto> relevantChunks = new ArrayList<>();
        if (request.getUseRAG()) {
            SearchQueryDto searchQuery = new SearchQueryDto();
            searchQuery.setUserId(request.getUserId());
            searchQuery.setQuery(request.getMessage());
            searchQuery.setTopK(3);
            searchQuery.setCategory(request.getCategory());

            relevantChunks = vectorStoreService.semanticSearch(searchQuery);
        }

        String fullPrompt = buildPromptWithContext(user, request.getMessage(), relevantChunks, conversation);

        String aiResponse = callGemini(fullPrompt);

        Message assistantMessage = new Message();
        assistantMessage.setConversation(conversation);
        assistantMessage.setRole("assistant");
        assistantMessage.setContent(aiResponse);

        List<Long> sourceIds = relevantChunks.stream()
                .map(SearchResultDto::getDocumentId)
                .distinct()
                .collect(Collectors.toList());
        assistantMessage.setSourcesUsed(gson.toJson(sourceIds));

        messageRepository.save(assistantMessage);

        conversation.setLastMessageAt(LocalDateTime.now());
        conversationRepository.save(conversation);

        ChatResponseDto response = new ChatResponseDto();
        response.setConversationId(conversation.getId());
        response.setMessageId(assistantMessage.getId());
        response.setResponse(aiResponse);
        response.setTimestamp(assistantMessage.getCreatedAt());
        response.setSources(buildSourceReferences(relevantChunks));

        return response;
    }

    private String buildPromptWithContext(User user, String userMessage,
                                          List<SearchResultDto> chunks,
                                          Conversation conversation) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("You are an intelligent AI tutor helping ");
        prompt.append(user.getUsername());
        prompt.append(" learn programming concepts.\n\n");

        List<KnowledgeState> weakSkills = knowledgeStateRepository
                .findByUserAndMasteryScoreLessThan(user, 50.0);

        if (!weakSkills.isEmpty()) {
            prompt.append("STUDENT'S WEAK AREAS:\n");
            prompt.append("The student needs help with: ");
            prompt.append(weakSkills.stream()
                    .map(ks -> ks.getSkill().getName())
                    .collect(Collectors.joining(", ")));
            prompt.append(".\n\n");
        }

        if (!chunks.isEmpty()) {
            prompt.append("CONTEXT FROM UPLOADED DOCUMENTS:\n");
            prompt.append("Use ONLY the following information to answer. ");
            prompt.append("If the answer isn't in this context, say so honestly.\n\n");

            for (int i = 0; i < chunks.size(); i++) {
                SearchResultDto chunk = chunks.get(i);
                prompt.append("--- Document ").append(i + 1).append(": ");
                prompt.append(chunk.getDocumentTitle()).append(" ---\n");
                prompt.append(chunk.getChunkContent()).append("\n\n");
            }
        }

        List<Message> history = messageRepository
                .findByConversationOrderByCreatedAtAsc(conversation);

        if (history.size() > 1) {
            prompt.append("CONVERSATION HISTORY:\n");
            int startIndex = Math.max(0, history.size() - 5);
            for (int i = startIndex; i < history.size() - 1; i++) {
                Message msg = history.get(i);
                prompt.append(msg.getRole().toUpperCase()).append(": ");
                prompt.append(msg.getContent()).append("\n");
            }
            prompt.append("\n");
        }

        prompt.append("INSTRUCTIONS:\n");
        prompt.append("- Answer clearly and concisely in 2-3 paragraphs\n");
        prompt.append("- If using document context, mention which document\n");
        prompt.append("- If information isn't in the context, say 'I don't have that information in the uploaded documents'\n");
        prompt.append("- Be encouraging and supportive\n");
        prompt.append("- Use examples when explaining concepts\n");
        prompt.append("- Focus on helping the student understand, not just giving answers\n\n");

        prompt.append("STUDENT'S QUESTION:\n");
        prompt.append(userMessage);

        return prompt.toString();
    }

    /**
     * UPDATED: Call Google Gemini API with logging
     */
    private String callGemini(String prompt) {
        try {
            JsonObject requestBody = new JsonObject();

            JsonArray contents = new JsonArray();
            JsonObject content = new JsonObject();
            JsonArray parts = new JsonArray();
            JsonObject part = new JsonObject();
            part.addProperty("text", prompt);
            parts.add(part);
            content.add("parts", parts);
            contents.add(content);
            requestBody.add("contents", contents);

            JsonObject generationConfig = new JsonObject();
            generationConfig.addProperty("temperature", temperature);
            generationConfig.addProperty("maxOutputTokens", maxTokens);
            requestBody.add("generationConfig", generationConfig);

            // Correct endpoint path
            String path = "/v1/models/" + model + ":generateContent?key=" + apiKey;

            String response = webClient.post()
                    .uri(path)
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody.toString())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            System.out.println("Gemini raw response: " + response);

            JsonObject jsonResponse = JsonParser.parseString(response).getAsJsonObject();

            if (jsonResponse.has("candidates")) {
                return jsonResponse.getAsJsonArray("candidates")
                        .get(0).getAsJsonObject()
                        .getAsJsonObject("content")
                        .getAsJsonArray("parts")
                        .get(0).getAsJsonObject()
                        .get("text").getAsString();
            } else if (jsonResponse.has("error")) {
                System.err.println("Gemini error: " + jsonResponse.get("error").toString());
                return "The AI service returned an error: "
                        + jsonResponse.get("error").getAsJsonObject()
                        .get("message").getAsString();
            } else {
                return "The AI service returned an unexpected response format.";
            }

        } catch (Exception e) {
            System.err.println("Gemini API error: " + e.getMessage());
            e.printStackTrace();
            return "I'm having trouble connecting to my AI service right now. Please check the server logs for details.";
        }
    }


    private String generateTitle(String message) {
        String title = message.substring(0, Math.min(50, message.length()));
        if (message.length() > 50) {
            title += "...";
        }
        return title;
    }

    private List<ChatResponseDto.SourceReference> buildSourceReferences(List<SearchResultDto> chunks) {
        return chunks.stream()
                .map(chunk -> {
                    ChatResponseDto.SourceReference ref = new ChatResponseDto.SourceReference();
                    ref.setDocumentId(chunk.getDocumentId());
                    ref.setDocumentTitle(chunk.getDocumentTitle());

                    String snippet = chunk.getChunkContent()
                            .substring(0, Math.min(150, chunk.getChunkContent().length()));
                    if (chunk.getChunkContent().length() > 150) {
                        snippet += "...";
                    }
                    ref.setSnippet(snippet);
                    ref.setRelevanceScore(chunk.getSimilarityScore());

                    return ref;
                })
                .collect(Collectors.toList());
    }

    public List<Message> getConversationHistory(Long conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));
        return messageRepository.findByConversationOrderByCreatedAtAsc(conversation);
    }

    public List<Conversation> getUserConversations(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return conversationRepository.findByUserOrderByLastMessageAtDesc(user);
    }
}

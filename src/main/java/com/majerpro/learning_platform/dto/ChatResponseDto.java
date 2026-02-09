package com.majerpro.learning_platform.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ChatResponseDto {

    private Long conversationId;
    private Long messageId;
    private String response;
    private LocalDateTime timestamp;
    private List<SourceReference> sources; // Documents used for answer

    @Data
    public static class SourceReference {
        private Long documentId;
        private String documentTitle;
        private String snippet; // Relevant excerpt
        private Double relevanceScore;
    }
}

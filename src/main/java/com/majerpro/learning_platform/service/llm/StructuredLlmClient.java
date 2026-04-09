package com.majerpro.learning_platform.service.llm;

import java.util.List;

public interface StructuredLlmClient {
    String generateHierarchyJson(String skillName);
    String summarizeNodeContent(String skillName, String nodeTitle, List<String> sourceChunks);
}
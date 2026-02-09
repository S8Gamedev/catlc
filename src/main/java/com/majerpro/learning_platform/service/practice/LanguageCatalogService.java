package com.majerpro.learning_platform.service.practice;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
public class LanguageCatalogService {

    private final Judge0Client judge0Client;

    private volatile List<Map<String, Object>> cached;
    private volatile Instant cachedAt;

    public LanguageCatalogService(Judge0Client judge0Client) {
        this.judge0Client = judge0Client;
    }

    public List<Map<String, Object>> getLanguages() {
        if (cached != null && cachedAt != null && Duration.between(cachedAt, Instant.now()).toMinutes() < 60) {
            return cached;
        }
        cached = judge0Client.getLanguages();
        cachedAt = Instant.now();
        return cached;
    }
}

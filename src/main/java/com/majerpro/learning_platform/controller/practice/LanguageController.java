package com.majerpro.learning_platform.controller.practice;

import com.majerpro.learning_platform.service.practice.LanguageCatalogService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/practice/code")
public class LanguageController {

    private final LanguageCatalogService languageCatalogService;

    public LanguageController(LanguageCatalogService languageCatalogService) {
        this.languageCatalogService = languageCatalogService;
    }

    @GetMapping("/languages")
    public List<Map<String, Object>> languages() {
        return languageCatalogService.getLanguages();
    }
}

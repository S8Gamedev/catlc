// NEW file: src/main/java/com/majerpro/learningplatform/controller/PracticeController.java
package com.majerpro.learning_platform.controller;

import com.majerpro.learning_platform.model.practice.CodeProblem;
import com.majerpro.learning_platform.repository.practice.CodeProblemRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/practice")
public class PracticeController {

    private final CodeProblemRepository codeProblemRepository;

    public PracticeController(CodeProblemRepository codeProblemRepository) {
        this.codeProblemRepository = codeProblemRepository;
    }

    @GetMapping
    public String listProblems(Model model) {
        List<CodeProblem> problems = codeProblemRepository.findAll();
        model.addAttribute("problems", problems);
        return "practice"; // templates/practice.html
    }

    @GetMapping("/{problemId}")
    public String problemDetail(@PathVariable Long problemId,
                                Principal principal,
                                Model model) {
        CodeProblem problem = codeProblemRepository.findById(problemId)
                .orElseThrow(() -> new RuntimeException("Problem not found: " + problemId));

        // Simple userId extraction; adjust if you store user in session instead of parsing name
        Long userId = 1L; // TODO: replace with real user lookup from Principal/session

        model.addAttribute("problem", problem);
        model.addAttribute("userId", userId);

        // Default languageId (Java = 62 in many Judge0 instances; adapt if needed)
        model.addAttribute("defaultLanguageId", 62);

        // Some starter code for the editor
        String boilerplate = """
                public class Main {
                    public static void main(String[] args) {
                        // Write your code here
                        System.out.println("Hello");
                    }
                }
                """;
        model.addAttribute("starterCode", boilerplate);

        return "practice-problem"; // templates/practice-problem.html
    }
}

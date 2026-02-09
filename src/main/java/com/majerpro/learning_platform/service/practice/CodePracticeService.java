package com.majerpro.learning_platform.service.practice;

import com.majerpro.learning_platform.dto.UpdateMasteryDto;
import com.majerpro.learning_platform.dto.practice.CodeSubmissionResultDto;
import com.majerpro.learning_platform.dto.practice.CreateCodeProblemDto;
import com.majerpro.learning_platform.dto.practice.SubmitCodeDto;
import com.majerpro.learning_platform.model.Skill;
import com.majerpro.learning_platform.model.User;
import com.majerpro.learning_platform.model.practice.CodeProblem;
import com.majerpro.learning_platform.model.practice.CodeSubmission;
import com.majerpro.learning_platform.model.practice.CodeTestCase; // NEW
import com.majerpro.learning_platform.repository.SkillRepository;
import com.majerpro.learning_platform.repository.UserRepository;
import com.majerpro.learning_platform.repository.practice.CodeProblemRepository;
import com.majerpro.learning_platform.repository.practice.CodeSubmissionRepository;
import com.majerpro.learning_platform.repository.practice.CodeTestCaseRepository; // NEW
import com.majerpro.learning_platform.service.KnowledgeTracingService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime; // NEW
import java.util.ArrayList;     // NEW
import java.util.List;          // NEW
import java.util.stream.Collectors; // NEW
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import java.util.stream.Collectors;

@Service
public class CodePracticeService {

    private final CodeProblemRepository codeProblemRepository;
    private final CodeSubmissionRepository codeSubmissionRepository;
    private final CodeTestCaseRepository codeTestCaseRepository; // NEW
    private final UserRepository userRepository;
    private final SkillRepository skillRepository;
    private final Judge0Client judge0Client;
    private final KnowledgeTracingService knowledgeTracingService; // optional update mastery

    private final int maxAttempts;
    private final long delayMs;

    public CodePracticeService(
            CodeProblemRepository codeProblemRepository,
            CodeSubmissionRepository codeSubmissionRepository,
            CodeTestCaseRepository codeTestCaseRepository, // NEW
            UserRepository userRepository,
            SkillRepository skillRepository,
            Judge0Client judge0Client,
            KnowledgeTracingService knowledgeTracingService,
            @Value("${judge0.poll.max-attempts:15}") int maxAttempts,
            @Value("${judge0.poll.delay-ms:700}") long delayMs
    ) {
        this.codeProblemRepository = codeProblemRepository;
        this.codeSubmissionRepository = codeSubmissionRepository;
        this.codeTestCaseRepository = codeTestCaseRepository; // NEW
        this.userRepository = userRepository;
        this.skillRepository = skillRepository;
        this.judge0Client = judge0Client;
        this.knowledgeTracingService = knowledgeTracingService;
        this.maxAttempts = maxAttempts;
        this.delayMs = delayMs;
    }

    @Transactional
    public CodeProblem createProblem(CreateCodeProblemDto dto) {
        CodeProblem p = new CodeProblem();
        if (dto.getSkillId() != null) {
            Skill skill = skillRepository.findById(dto.getSkillId())
                    .orElseThrow(() -> new RuntimeException("Skill not found"));
            p.setSkill(skill);
        }
        p.setTitle(dto.getTitle());
        p.setPrompt(dto.getPrompt());
        p.setCategory(dto.getCategory());
        p.setDifficultyLevel(dto.getDifficultyLevel());
        p.setSampleInput(dto.getSampleInput());
        p.setExpectedOutput(dto.getExpectedOutput());
        return codeProblemRepository.save(p);
    }

    @Transactional
    public CodeSubmissionResultDto submit(SubmitCodeDto dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        CodeProblem problem = codeProblemRepository.findById(dto.getProblemId())
                .orElseThrow(() -> new RuntimeException("Problem not found"));

        // NEW: Load all test cases for the problem
        List<CodeTestCase> cases = codeTestCaseRepository.findByProblem(problem); // NEW
        // If empty, auto-create a minimal testcase from CodeProblem itself
        if (cases == null || cases.isEmpty()) {
            if (problem.getExpectedOutput() == null || problem.getExpectedOutput().isBlank()) {
                throw new RuntimeException("No test cases and no expectedOutput; cannot grade.");
            }
            CodeTestCase tc = new CodeTestCase();
            tc.setProblem(problem);
            tc.setStdin(problem.getSampleInput() == null ? "" : problem.getSampleInput());
            tc.setExpectedOutput(problem.getExpectedOutput());
            tc.setIsSample(true);
            tc.setWeight(1);
            codeTestCaseRepository.save(tc);

            cases = List.of(tc);
        }

        // NEW: Build Judge0 batch request (one submission per testcase)
        Judge0Client.BatchSubmitRequest batchReq = new Judge0Client.BatchSubmitRequest(); // NEW
        List<Judge0Client.SubmitRequest> submissions = new ArrayList<>(); // NEW

        for (CodeTestCase tc : cases) { // NEW
            Judge0Client.SubmitRequest req = new Judge0Client.SubmitRequest(); // NEW
            req.setLanguage_id(dto.getLanguageId()); // NEW
            req.setSource_code(dto.getSourceCode()); // NEW
            req.setStdin(tc.getStdin()); // NEW
            req.setExpected_output(tc.getExpectedOutput()); // NEW (Judge0 compares stdout vs expected_output) [attached_file:1]
            req.setEnable_network(false); // NEW (recommended safety for deployed) [attached_file:1]
            submissions.add(req); // NEW
        }
        batchReq.setSubmissions(submissions); // NEW

        // NEW: Call Judge0 batch create -> returns tokens
        List<Judge0Client.BatchSubmitResponseItem> tokenItems = judge0Client.createBatch(batchReq); // NEW
        String tokensCsv = tokenItems.stream().map(Judge0Client.BatchSubmitResponseItem::getToken)
                .collect(Collectors.joining(",")); // NEW

        // CHANGED: Save local submission immediately (store judgeTokensCsv instead of judgeToken)
        CodeSubmission sub = new CodeSubmission();
        sub.setUser(user);
        sub.setProblem(problem);
        sub.setLanguageId(dto.getLanguageId());
        sub.setSourceCode(dto.getSourceCode());
        sub.setStdin(null); // CHANGED: stdin now comes from each test case
        sub.setJudgeTokensCsv(tokensCsv); // NEW
        sub.setStatus("QUEUED");
        sub.setIsCorrect(false);
        sub.setScore(0.0);
        sub.setPassedCount(0); // NEW
        sub.setTotalCount(cases.size()); // NEW

        CodeSubmission saved = codeSubmissionRepository.save(sub);
        return toDto(saved);
    }

    @Transactional
    public CodeSubmissionResultDto evaluate(Long submissionId) {
        CodeSubmission sub = codeSubmissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("Submission not found"));

        if (sub.getJudgeTokensCsv() == null || sub.getJudgeTokensCsv().isBlank()) { // NEW
            throw new RuntimeException("No Judge0 batch tokens found for this submission"); // NEW
        }

        // NEW: Poll Judge0 batch until all finished
        Judge0Client.BatchGetResponse batch = null; // NEW
        for (int i = 0; i < maxAttempts; i++) { // NEW
            batch = judge0Client.getBatch(sub.getJudgeTokensCsv()); // NEW
            if (batch == null || batch.getSubmissions() == null) { // NEW
                sleep(delayMs); // NEW
                continue; // NEW
            }

            boolean anyRunning = batch.getSubmissions().stream().anyMatch(r -> { // NEW
                String s = (r.getStatus() != null) ? r.getStatus().getDescription() : "UNKNOWN"; // NEW
                return "In Queue".equalsIgnoreCase(s) || "Processing".equalsIgnoreCase(s); // NEW
            });

            if (!anyRunning) { // NEW
                break; // NEW
            }
            sleep(delayMs); // NEW
        }

        if (batch == null || batch.getSubmissions() == null) { // NEW
            throw new RuntimeException("Judge0 batch result not available"); // NEW
        }

        // NEW: Compute pass/fail using Judge0 status (Accepted vs others)
        int total = batch.getSubmissions().size(); // NEW
        int passed = 0; // NEW

        String firstStdout = null; // NEW (optional)
        String firstStderr = null; // NEW (optional)
        String firstCompile = null; // NEW (optional)

        StringBuilder report = new StringBuilder(); // NEW
        report.append("["); // NEW

        for (int i = 0; i < batch.getSubmissions().size(); i++) { // NEW
            Judge0Client.SubmissionResult r = batch.getSubmissions().get(i); // NEW
            String status = (r.getStatus() != null) ? r.getStatus().getDescription() : "UNKNOWN"; // NEW

            boolean ok = "Accepted".equalsIgnoreCase(status); // NEW (Judge0 status “Accepted”) [attached_file:1]
            if (ok) passed++; // NEW

            if (firstStdout == null && r.getStdout() != null) firstStdout = r.getStdout(); // NEW
            if (firstStderr == null && r.getStderr() != null) firstStderr = r.getStderr(); // NEW
            if (firstCompile == null && r.getCompile_output() != null) firstCompile = r.getCompile_output(); // NEW

            // lightweight JSON-ish report for UI/debug (no extra libs)
            report.append("{\"token\":\"").append(r.getToken()).append("\",") // NEW
                    .append("\"status\":\"").append(status).append("\"}"); // NEW
            if (i < batch.getSubmissions().size() - 1) report.append(","); // NEW
        }
        report.append("]"); // NEW

        double score = (total == 0) ? 0.0 : (passed * 100.0 / total); // NEW
        boolean correct = (total > 0 && passed == total); // NEW

        // CHANGED: Store outputs + summary
        sub.setPassedCount(passed); // NEW
        sub.setTotalCount(total); // NEW
        sub.setTestcaseReport(report.toString()); // NEW
        sub.setStdout(firstStdout); // CHANGED (optional)
        sub.setStderr(firstStderr); // CHANGED (optional)
        sub.setCompileOutput(firstCompile); // CHANGED (optional)
        sub.setStatus(correct ? "Accepted" : "Wrong Answer"); // CHANGED
        sub.setIsCorrect(correct); // CHANGED
        sub.setScore(score); // CHANGED
        sub.setEvaluatedAt(LocalDateTime.now()); // CHANGED

        CodeSubmission saved = codeSubmissionRepository.save(sub);

        // Optional mastery update if problem has a Skill (scaled by score)
        if (saved.getProblem().getSkill() != null && saved.getScore() != null && saved.getScore() > 0) { // NEW
            UpdateMasteryDto masteryDto = new UpdateMasteryDto();
            masteryDto.setUserId(saved.getUser().getId());
            masteryDto.setSkillId(saved.getProblem().getSkill().getId());
            masteryDto.setLearningGain(10.0 * (saved.getScore() / 100.0)); // NEW (scale gain)
            knowledgeTracingService.updateMastery(masteryDto);
        }

        return toDto(saved);
    }

    // NEW
    @Transactional(readOnly = true)
    public java.util.List<CodeSubmissionResultDto> getSubmissionsForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return codeSubmissionRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(CodePracticeService::toDto)
                .collect(Collectors.toList());
    }

    // NEW
    @Transactional(readOnly = true)
    public java.util.List<CodeSubmissionResultDto> getSubmissionsForUserAndProblem(Long userId, Long problemId, int limit) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        CodeProblem problem = codeProblemRepository.findById(problemId)
                .orElseThrow(() -> new RuntimeException("Problem not found"));

        int safeLimit = (limit <= 0) ? 20 : Math.min(limit, 100); // NEW (protect API)

        var page = PageRequest.of(0, safeLimit, Sort.by(Sort.Direction.DESC, "createdAt")); // NEW

        return codeSubmissionRepository.findByUserAndProblem(user, problem, page)
                .stream()
                .map(CodePracticeService::toDto)
                .collect(Collectors.toList());
    }


    private static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }

    private static CodeSubmissionResultDto toDto(CodeSubmission sub) {
        CodeSubmissionResultDto dto = new CodeSubmissionResultDto();
        dto.setSubmissionId(sub.getId());
        dto.setProblemId(sub.getProblem().getId());

        // CHANGED: CodeSubmissionResultDto currently has "judgeToken".
        // For batch mode, return judgeTokensCsv in that field until you rename DTO.
        dto.setJudgeToken(sub.getJudgeTokensCsv()); // CHANGED

        dto.setStatus(sub.getStatus());
        dto.setIsCorrect(sub.getIsCorrect());
        dto.setScore(sub.getScore());
        dto.setStdout(sub.getStdout());
        dto.setStderr(sub.getStderr());
        dto.setCompileOutput(sub.getCompileOutput());
        dto.setCreatedAt(sub.getCreatedAt());
        dto.setEvaluatedAt(sub.getEvaluatedAt());
        return dto;
    }
}

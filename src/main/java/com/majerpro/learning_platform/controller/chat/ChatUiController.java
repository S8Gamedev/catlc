package com.majerpro.learning_platform.controller.chat;

import com.majerpro.learning_platform.dto.chat.ChatMessageDto;
import com.majerpro.learning_platform.dto.chat.ChatSelectedSkillDto;
import com.majerpro.learning_platform.dto.chat.ChatSkillListItemDto;
import com.majerpro.learning_platform.model.Skill;
import com.majerpro.learning_platform.model.content.SkillContent;
import com.majerpro.learning_platform.model.UserSkillProgress;
import com.majerpro.learning_platform.repository.content.SkillContentRepository;
import com.majerpro.learning_platform.repository.UserSkillProgressRepository;
import com.majerpro.learning_platform.service.progress.ProgressTrackingService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Controller
@RequestMapping("/chat")
@SessionAttributes("chatMessages")
public class ChatUiController {

    private final UserSkillProgressRepository progressRepository;
    private final SkillContentRepository skillContentRepository;
    private final ProgressTrackingService progressTrackingService;

    public ChatUiController(UserSkillProgressRepository progressRepository,
                            SkillContentRepository skillContentRepository,
                            ProgressTrackingService progressTrackingService) {
        this.progressRepository = progressRepository;
        this.skillContentRepository = skillContentRepository;
        this.progressTrackingService = progressTrackingService;
    }

    @ModelAttribute("chatMessages")
    public List<ChatMessageDto> chatMessages() {
        return new ArrayList<>();
    }

    @GetMapping({"", "/"})
    public String chatOverview(@RequestParam(defaultValue = "1") Long userId,
                               @RequestParam(required = false) Long skillId,
                               @ModelAttribute("chatMessages") List<ChatMessageDto> chatMessages,
                               Model model) {

        List<UserSkillProgress> trackedSkills = progressRepository.findByUserId(userId);

        List<ChatSkillListItemDto> skills = trackedSkills.stream()
                .map(progress -> {
                    ChatSkillListItemDto dto = new ChatSkillListItemDto();
                    dto.setSkillId(progress.getSkill().getId());
                    dto.setSkillName(progress.getSkill().getName());
                    dto.setDescription(progress.getSkill().getDescription());
                    return dto;
                })
                .sorted(Comparator.comparing(ChatSkillListItemDto::getSkillName, String.CASE_INSENSITIVE_ORDER))
                .toList();

        model.addAttribute("skills", skills);
        model.addAttribute("userId", userId);

        if (skillId == null) {
            model.addAttribute("selectedSkill", null);
            model.addAttribute("messages", List.of());
            return "chat/overview";
        }

        UserSkillProgress progress = progressRepository.findByUserIdAndSkillId(userId, skillId)
                .orElseThrow(() -> new RuntimeException("Tracked skill not found for user"));

        ChatSelectedSkillDto selectedSkill = new ChatSelectedSkillDto();
        selectedSkill.setSkillId(progress.getSkill().getId());
        selectedSkill.setSkillName(progress.getSkill().getName());
        selectedSkill.setDescription(progress.getSkill().getDescription());

        model.addAttribute("selectedSkill", selectedSkill);
        model.addAttribute("messages", chatMessages);

        return "chat/overview";
    }

    @PostMapping("/send")
    public String sendMessage(@RequestParam Long skillId,
                              @RequestParam(defaultValue = "1") Long userId,
                              @RequestParam String prompt,
                              @ModelAttribute("chatMessages") List<ChatMessageDto> chatMessages) {

        if (prompt != null && !prompt.isBlank()) {
            UserSkillProgress progress = progressRepository.findByUserIdAndSkillId(userId, skillId)
                    .orElseThrow(() -> new RuntimeException("Tracked skill not found for user"));

            Skill skill = progress.getSkill();
            SkillContent storedContent = skillContentRepository.findBySkillId(skillId).orElse(null);

            ChatMessageDto userMessage = new ChatMessageDto();
            userMessage.setRole("USER");
            userMessage.setContent(prompt.trim());
            chatMessages.add(userMessage);

            String assistantReply = buildAssistantReply(skill, storedContent, prompt.trim());

            ChatMessageDto assistantMessage = new ChatMessageDto();
            assistantMessage.setRole("ASSISTANT");
            assistantMessage.setContent(assistantReply);
            chatMessages.add(assistantMessage);

            progressTrackingService.recordPractice(userId, skillId, 75.0);
        }

        return "redirect:/chat?skillId=" + skillId + "&userId=" + userId;
    }

    private String buildAssistantReply(Skill skill, SkillContent storedContent, String prompt) {
        StringBuilder reply = new StringBuilder();

        reply.append("You are asking about ").append(skill.getName()).append(".\n\n");

        if (storedContent != null && storedContent.getSummary() != null && !storedContent.getSummary().isBlank()) {
            reply.append("Stored summary: ").append(storedContent.getSummary()).append("\n\n");
        }

        if (storedContent != null && storedContent.getContent() != null && !storedContent.getContent().isBlank()) {
            String content = storedContent.getContent();
            String shortened = content.length() > 500 ? content.substring(0, 500) + "..." : content;
            reply.append("Relevant notes:\n").append(shortened).append("\n\n");
        }

        reply.append("Response to your prompt:\n");
        reply.append("“").append(prompt).append("”\n\n");
        reply.append("This is currently a starter tutor response. The next step is connecting this controller to your actual tool/AI backend so the reply becomes fully dynamic and conversational.");

        return reply.toString();
    }
}
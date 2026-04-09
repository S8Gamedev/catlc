package com.majerpro.learning_platform.service.content;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.majerpro.learning_platform.dto.content.HierarchyNodeDto;
import com.majerpro.learning_platform.dto.content.HierarchyTreeDto;
import com.majerpro.learning_platform.dto.content.NodeSummaryDto;
import com.majerpro.learning_platform.model.Skill;
import com.majerpro.learning_platform.model.content.IngestionRun;
import com.majerpro.learning_platform.model.content.NodeContent;
import com.majerpro.learning_platform.model.content.SkillNode;
import com.majerpro.learning_platform.model.content.SourceDocument;
import com.majerpro.learning_platform.repository.SkillRepository;
import com.majerpro.learning_platform.repository.content.IngestionRunRepository;
import com.majerpro.learning_platform.repository.content.NodeContentRepository;
import com.majerpro.learning_platform.repository.content.SkillNodeRepository;
import com.majerpro.learning_platform.repository.content.SourceDocumentRepository;
import com.majerpro.learning_platform.service.llm.StructuredLlmClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ContentIngestionService {

    private final SkillRepository skillRepository;
    private final SkillNodeRepository skillNodeRepository;
    private final IngestionRunRepository ingestionRunRepository;
    private final SourceDocumentRepository sourceDocumentRepository;
    private final NodeContentRepository nodeContentRepository;
    private final StructuredLlmClient structuredLlmClient;
    private final JsoupScrapeService jsoupScrapeService;
    private final SkillRagBridgeService skillRagBridgeService;
    private final ObjectMapper objectMapper;

    public ContentIngestionService(SkillRepository skillRepository,
                                   SkillNodeRepository skillNodeRepository,
                                   IngestionRunRepository ingestionRunRepository,
                                   SourceDocumentRepository sourceDocumentRepository,
                                   NodeContentRepository nodeContentRepository,
                                   StructuredLlmClient structuredLlmClient,
                                   JsoupScrapeService jsoupScrapeService,
                                   SkillRagBridgeService skillRagBridgeService,
                                   ObjectMapper objectMapper) {
        this.skillRepository = skillRepository;
        this.skillNodeRepository = skillNodeRepository;
        this.ingestionRunRepository = ingestionRunRepository;
        this.sourceDocumentRepository = sourceDocumentRepository;
        this.nodeContentRepository = nodeContentRepository;
        this.structuredLlmClient = structuredLlmClient;
        this.jsoupScrapeService = jsoupScrapeService;
        this.skillRagBridgeService = skillRagBridgeService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public Long ingestSkillById(Long skillId) {
        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new RuntimeException("Skill not found"));
        return ingest(skill);
    }

    @Transactional
    public Long ingestSkillByName(String rawSkillName) {
        String normalized = normalizeSkillName(rawSkillName);

        Skill skill = skillRepository.findByNameIgnoreCase(normalized)
                .orElseGet(() -> {
                    Skill newSkill = new Skill();
                    newSkill.setName(normalized);
                    try {
                        newSkill.setDescription("Auto-created skill for AI content ingestion.");
                    } catch (Exception ignored) {
                    }
                    return skillRepository.save(newSkill);
                });

        return ingest(skill);
    }

    private Long ingest(Skill skill) {
        IngestionRun run = new IngestionRun();
        run.setSkill(skill);
        run.setStatus(IngestionRun.Status.STARTED);
        run.setLlmProvider("GROQ");
        run.setLlmModel("llama-3.1-8b-instant");
        run.setStartedAt(LocalDateTime.now());
        run = ingestionRunRepository.save(run);

        try {
            skillNodeRepository.deleteBySkillId(skill.getId());

            String hierarchyJson = structuredLlmClient.generateHierarchyJson(skill.getName());
            HierarchyTreeDto tree = objectMapper.readValue(hierarchyJson, HierarchyTreeDto.class);

            int order = 0;
            for (HierarchyNodeDto nodeDto : tree.getNodes()) {
                saveNodeRecursive(skill, null, nodeDto, 0, order++);
            }

            run.setStatus(IngestionRun.Status.TREE_GENERATED);
            ingestionRunRepository.save(run);

            List<SkillNode> savedNodes = skillNodeRepository.findBySkillIdOrderByDepthAscNodeOrderAsc(skill.getId());

            for (SkillNode node : savedNodes) {
                List<String> urls = buildSeedUrls(skill.getName(), node.getTitle());

                for (String url : urls) {
                    JsoupScrapeService.ScrapedPage scraped = jsoupScrapeService.scrape(url);

                    SourceDocument sourceDocument = new SourceDocument();
                    sourceDocument.setIngestionRun(run);
                    sourceDocument.setSkillNode(node);
                    sourceDocument.setUrl(scraped.getUrl());
                    sourceDocument.setDomain(scraped.getDomain());
                    sourceDocument.setTitle(scraped.getTitle());
                    sourceDocument.setStatusCode(scraped.getStatusCode());
                    sourceDocument.setFetchedAt(scraped.getFetchedAt());
                    sourceDocument.setRawText(scraped.getRawText());
                    sourceDocument.setCleanedText(scraped.getCleanedText());
                    sourceDocument.setRankingScore(score(scraped, node));
                    sourceDocumentRepository.save(sourceDocument);
                }

                List<SourceDocument> rankedSources = sourceDocumentRepository
                        .findBySkillNodeIdOrderByRankingScoreDesc(node.getId());

                if (!rankedSources.isEmpty()) {
                    SourceDocument best = rankedSources.get(0);

                    String summaryJson = structuredLlmClient.summarizeNodeContent(
                            skill.getName(),
                            node.getTitle(),
                            List.of(best.getCleanedText())
                    );

                    NodeSummaryDto summaryDto = objectMapper.readValue(summaryJson, NodeSummaryDto.class);

                    skillRagBridgeService.storeSkillNodeInRag(
                            1L,
                            skill,
                            node,
                            run,
                            summaryDto.getSummary(),
                            summaryDto.getKeyPoints(),
                            summaryDto.getExample(),
                            best.getUrl()
                    );

                    NodeContent nodeContent = nodeContentRepository.findBySkillNodeId(node.getId())
                            .orElseGet(NodeContent::new);

                    nodeContent.setSkillNode(node);
                    nodeContent.setSourceDocument(best);
                    nodeContent.setSummary(summaryDto.getSummary());
                    nodeContent.setKeyPoints(String.join("\n", summaryDto.getKeyPoints()));
                    nodeContent.setExample(summaryDto.getExample());

                    nodeContentRepository.save(nodeContent);
                }
            }

            run.setStatus(IngestionRun.Status.COMPLETED);
            run.setCompletedAt(LocalDateTime.now());
            ingestionRunRepository.save(run);

            return run.getId();
        } catch (Exception e) {
            run.setStatus(IngestionRun.Status.FAILED);
            run.setCompletedAt(LocalDateTime.now());
            run.setErrorMessage(e.getMessage());
            ingestionRunRepository.save(run);
            throw new RuntimeException("Skill ingestion failed", e);
        }
    }

    private void saveNodeRecursive(Skill skill,
                                   SkillNode parent,
                                   HierarchyNodeDto dto,
                                   int depth,
                                   int order) {
        SkillNode node = new SkillNode();
        node.setSkill(skill);
        node.setParent(parent);
        node.setTitle(dto.getTitle());
        node.setObjective(dto.getObjective());
        node.setDepth(depth);
        node.setNodeOrder(order);

        SkillNode saved = skillNodeRepository.save(node);

        int childOrder = 0;
        for (HierarchyNodeDto child : dto.getChildren()) {
            saveNodeRecursive(skill, saved, child, depth + 1, childOrder++);
        }
    }

    private String normalizeSkillName(String rawSkillName) {
        if (rawSkillName == null || rawSkillName.isBlank()) {
            throw new IllegalArgumentException("Skill name must not be blank");
        }
        return rawSkillName.trim().replaceAll("\\s+", " ");
    }

    private List<String> buildSeedUrls(String skillName, String nodeTitle) {
        return List.of(
                "https://www.baeldung.com/",
                "https://www.geeksforgeeks.org/",
                "https://docs.oracle.com/"
        );
    }

    private double score(JsoupScrapeService.ScrapedPage page, SkillNode node) {
        double score = 0.0;

        if (page.getTitle() != null &&
                page.getTitle().toLowerCase().contains(node.getTitle().toLowerCase())) {
            score += 50.0;
        }

        if (page.getCleanedText() != null && page.getCleanedText().length() > 500) {
            score += 25.0;
        }

        if (page.getDomain() != null) {
            if (page.getDomain().contains("oracle")) score += 15.0;
            if (page.getDomain().contains("baeldung")) score += 10.0;
            if (page.getDomain().contains("geeksforgeeks")) score += 8.0;
        }

        return score;
    }
}
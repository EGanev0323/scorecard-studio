package com.scorecardstudio.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.scorecardstudio.scoring.EvaluationResult;
import com.scorecardstudio.submission.SubmissionService;
import com.scorecardstudio.template.ScorecardTemplate;
import com.scorecardstudio.template.TemplateRegistry;
import com.scorecardstudio.web.ApiDtos.EvaluateRequest;
import com.scorecardstudio.web.ApiDtos.TemplateSummary;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/templates")
@Tag(name = "Templates", description = "Scorecard definitions loaded from configuration")
public class TemplateController {

    private final TemplateRegistry registry;
    private final SubmissionService submissions;

    public TemplateController(TemplateRegistry registry, SubmissionService submissions) {
        this.registry = registry;
        this.submissions = submissions;
    }

    @GetMapping
    @Operation(summary = "List available scorecard templates")
    public List<TemplateSummary> list() {
        return registry.all().stream().map(TemplateSummary::of).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a full template: questions, rules, thresholds and branding")
    public ScorecardTemplate get(@PathVariable String id) {
        return registry.get(id);
    }

    @PostMapping("/{id}/evaluate")
    @Operation(summary = "Score partial answers for the live preview (nothing is saved)")
    public EvaluationResult evaluate(@PathVariable String id, @Valid @RequestBody EvaluateRequest request) {
        return submissions.preview(id, request.answers());
    }
}

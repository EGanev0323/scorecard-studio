package com.scorecardstudio.submission;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.scorecardstudio.scoring.EvaluationResult;
import com.scorecardstudio.scoring.ScoringEngine;
import com.scorecardstudio.template.ScorecardTemplate;
import com.scorecardstudio.template.TemplateRegistry;

@Service
public class SubmissionService {

    private final TemplateRegistry templates;
    private final ScoringEngine scoring;
    private final SubmissionRepository repository;

    public SubmissionService(TemplateRegistry templates, ScoringEngine scoring, SubmissionRepository repository) {
        this.templates = templates;
        this.scoring = scoring;
        this.repository = repository;
    }

    public EvaluationResult preview(String templateId, Map<String, Object> answers) {
        return scoring.evaluate(templates.get(templateId), answers, false);
    }

    @Transactional
    public Submission submit(String templateId, String respondentName, String respondentOrg, Map<String, Object> answers) {
        ScorecardTemplate template = templates.get(templateId);
        EvaluationResult result = scoring.evaluate(template, answers, true);
        return repository.save(new Submission(template, respondentName.strip(),
                respondentOrg == null || respondentOrg.isBlank() ? null : respondentOrg.strip(), answers, result));
    }

    @Transactional(readOnly = true)
    public Submission get(UUID id) {
        return repository.findById(id).orElseThrow(() -> new SubmissionNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<Submission> recent(String templateId, int limit) {
        PageRequest page = PageRequest.of(0, Math.clamp(limit, 1, 100));
        return templateId == null || templateId.isBlank()
                ? repository.findAllByOrderByCreatedAtDesc(page)
                : repository.findByTemplateIdOrderByCreatedAtDesc(templateId, page);
    }
}

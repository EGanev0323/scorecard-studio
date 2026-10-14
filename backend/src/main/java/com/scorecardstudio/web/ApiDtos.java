package com.scorecardstudio.web;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import com.scorecardstudio.scoring.EvaluationResult;
import com.scorecardstudio.submission.Submission;
import com.scorecardstudio.template.ScorecardTemplate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

final class ApiDtos {

    private ApiDtos() {
    }

    record TemplateSummary(String id, String version, String name, String description,
                           ScorecardTemplate.Branding branding, int categoryCount, int questionCount) {

        static TemplateSummary of(ScorecardTemplate t) {
            int questions = t.categories().stream().mapToInt(c -> c.questions().size()).sum();
            return new TemplateSummary(t.id(), t.version(), t.name(), t.description(), t.branding(),
                    t.categories().size(), questions);
        }
    }

    record EvaluateRequest(@NotNull @Size(max = 500) Map<String, Object> answers) {
    }

    record SubmitRequest(
            @NotBlank @Size(max = 120) String respondentName,
            @Size(max = 160) String respondentOrg,
            @NotNull @Size(max = 500) Map<String, Object> answers) {
    }

    record SubmissionSummary(UUID id, String templateId, String templateName, String respondentName,
                             String respondentOrg, Integer overallScore, String statusLabel,
                             String statusColor, Instant createdAt) {

        static SubmissionSummary of(Submission s) {
            EvaluationResult.Status status = s.getResult().status();
            return new SubmissionSummary(s.getId(), s.getTemplateId(), s.getTemplateName(), s.getRespondentName(),
                    s.getRespondentOrg(), s.getOverallScore(), s.getStatusLabel(),
                    status == null ? null : status.color(), s.getCreatedAt());
        }
    }

    record SubmissionDetail(UUID id, ScorecardTemplate template, String respondentName, String respondentOrg,
                            Map<String, Object> answers, EvaluationResult result, Instant createdAt) {

        static SubmissionDetail of(Submission s) {
            return new SubmissionDetail(s.getId(), s.getTemplateSnapshot(), s.getRespondentName(), s.getRespondentOrg(),
                    s.getAnswers(), s.getResult(), s.getCreatedAt());
        }
    }
}

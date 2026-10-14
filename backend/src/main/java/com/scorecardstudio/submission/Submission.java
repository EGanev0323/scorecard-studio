package com.scorecardstudio.submission;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.scorecardstudio.scoring.EvaluationResult;
import com.scorecardstudio.template.ScorecardTemplate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * A completed scorecard. The template is stored as a snapshot next to the answers and result,
 * so a report can be regenerated exactly as it was even after the template config changes.
 */
@Entity
@Table(name = "submission")
public class Submission {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String templateId;

    private String templateVersion;

    @Column(nullable = false)
    private String templateName;

    @Column(nullable = false)
    private String respondentName;

    private String respondentOrg;

    private Integer overallScore;

    private String statusLabel;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false)
    private Map<String, Object> answers;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false)
    private EvaluationResult result;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false)
    private ScorecardTemplate templateSnapshot;

    @Column(nullable = false)
    private Instant createdAt;

    protected Submission() {
    }

    public Submission(ScorecardTemplate template, String respondentName, String respondentOrg,
                      Map<String, Object> answers, EvaluationResult result) {
        this.id = UUID.randomUUID();
        this.templateId = template.id();
        this.templateVersion = template.version();
        this.templateName = template.name();
        this.respondentName = respondentName;
        this.respondentOrg = respondentOrg;
        this.overallScore = result.overallScore();
        this.statusLabel = result.status() == null ? null : result.status().label();
        this.answers = answers;
        this.result = result;
        this.templateSnapshot = template;
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public String getTemplateId() { return templateId; }
    public String getTemplateVersion() { return templateVersion; }
    public String getTemplateName() { return templateName; }
    public String getRespondentName() { return respondentName; }
    public String getRespondentOrg() { return respondentOrg; }
    public Integer getOverallScore() { return overallScore; }
    public String getStatusLabel() { return statusLabel; }
    public Map<String, Object> getAnswers() { return answers; }
    public EvaluationResult getResult() { return result; }
    public ScorecardTemplate getTemplateSnapshot() { return templateSnapshot; }
    public Instant getCreatedAt() { return createdAt; }
}

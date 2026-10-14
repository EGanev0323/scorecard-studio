package com.scorecardstudio.scoring;

import java.util.List;

/**
 * Outcome of scoring a set of answers against a template. Scores are 0–100; a category or the
 * overall score is {@code null} until at least one of its scored questions has been answered.
 */
public record EvaluationResult(
        String templateId,
        String templateVersion,
        Integer overallScore,
        Status status,
        int answeredQuestions,
        int totalQuestions,
        int completionPercent,
        List<CategoryResult> categories) {

    public record Status(String id, String label, String color, String summary) {
    }

    public record CategoryResult(String id, String name, Integer score, Status status,
                                 int answeredQuestions, int totalQuestions, String recommendation) {
    }
}

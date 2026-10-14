package com.scorecardstudio.template;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * A complete scorecard definition: questions, scoring rules, thresholds, branding and report copy.
 * Templates are plain JSON files, so a new assessment is a new file, not new code.
 */
public record ScorecardTemplate(
        String id,
        String version,
        String name,
        String description,
        Branding branding,
        List<Category> categories,
        List<Threshold> thresholds,
        Report report) {

    public record Branding(String organization, String tagline, String logoText,
                           String primaryColor, String accentColor) {
    }

    public record Category(String id, String name, String description, Double weight,
                           List<Question> questions, Map<String, String> recommendations) {

        public double effectiveWeight() {
            return weight == null ? 1.0 : weight;
        }
    }

    public record Question(String id, String label, String help, QuestionType type,
                           boolean required, Double weight, Integer min, Integer max,
                           String minLabel, String maxLabel, boolean invert,
                           List<Option> options, List<Band> bands, String unit) {

        public double effectiveWeight() {
            return weight == null ? 1.0 : weight;
        }

        public boolean scored() {
            return type != QuestionType.TEXT;
        }
    }

    public record Option(String value, String label, double score) {
    }

    /** Numeric band: values in [from, to) score {@code score}. Null bounds are open-ended. */
    public record Band(Double from, Double to, double score) {

        public boolean contains(double value) {
            return (from == null || value >= from) && (to == null || value < to);
        }
    }

    /** Status band. A score belongs to the highest threshold whose {@code min} it reaches. */
    public record Threshold(String id, String label, double min, String color, String summary) {
    }

    public record Report(String title, String intro, String footer) {
    }

    @JsonIgnore
    public Map<String, Question> questionsById() {
        return categories.stream()
                .flatMap(c -> c.questions().stream())
                .collect(Collectors.toMap(Question::id, Function.identity()));
    }

    @JsonIgnore
    public Optional<Category> category(String categoryId) {
        return categories.stream().filter(c -> c.id().equals(categoryId)).findFirst();
    }
}

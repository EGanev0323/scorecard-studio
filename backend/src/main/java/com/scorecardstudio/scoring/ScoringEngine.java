package com.scorecardstudio.scoring;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.scorecardstudio.scoring.EvaluationResult.CategoryResult;
import com.scorecardstudio.scoring.EvaluationResult.Status;
import com.scorecardstudio.template.ScorecardTemplate;
import com.scorecardstudio.template.ScorecardTemplate.Band;
import com.scorecardstudio.template.ScorecardTemplate.Category;
import com.scorecardstudio.template.ScorecardTemplate.Option;
import com.scorecardstudio.template.ScorecardTemplate.Question;
import com.scorecardstudio.template.ScorecardTemplate.Threshold;

/**
 * Validates answers and turns them into category and overall scores using only the rules declared
 * in the template. There is no expression language or scripting in configs: every rule is data
 * (ranges, option scores, numeric bands, weights), so a template cannot execute code.
 */
@Component
public class ScoringEngine {

    static final int MAX_TEXT_LENGTH = 2000;

    /**
     * @param requireComplete {@code true} when submitting a final report (required questions must be
     *                        answered); {@code false} for the live preview while the form is filled in.
     */
    public EvaluationResult evaluate(ScorecardTemplate template, Map<String, Object> answers, boolean requireComplete) {
        Map<String, Object> given = answers == null ? Map.of() : answers;
        List<AnswerError> errors = new ArrayList<>();

        Map<String, Question> known = template.questionsById();
        given.keySet().stream()
                .filter(id -> !known.containsKey(id))
                .sorted()
                .forEach(id -> errors.add(new AnswerError(id, "Unknown question")));

        List<CategoryResult> categoryResults = new ArrayList<>();
        double weightedSum = 0;
        double weightTotal = 0;
        int answeredTotal = 0;
        int questionTotal = 0;

        for (Category category : template.categories()) {
            double catSum = 0;
            double catWeight = 0;
            int answered = 0;
            int scoredQuestions = 0;
            for (Question q : category.questions()) {
                Object raw = given.get(q.id());
                boolean present = !isEmpty(raw);
                if (!present) {
                    if (requireComplete && q.required()) {
                        errors.add(new AnswerError(q.id(), "This question is required"));
                    }
                    if (q.scored()) {
                        scoredQuestions++;
                    }
                    continue;
                }
                Double score = scoreQuestion(q, raw, errors);
                if (!q.scored()) {
                    continue;
                }
                scoredQuestions++;
                if (score != null) {
                    answered++;
                    catSum += score * q.effectiveWeight();
                    catWeight += q.effectiveWeight();
                }
            }
            Integer catScore = catWeight > 0 ? (int) Math.round(catSum / catWeight) : null;
            Status catStatus = catScore == null ? null : status(template, catScore);
            String recommendation = catStatus == null || category.recommendations() == null
                    ? null : category.recommendations().get(catStatus.id());
            categoryResults.add(new CategoryResult(category.id(), category.name(), catScore, catStatus,
                    answered, scoredQuestions, recommendation));

            answeredTotal += answered;
            questionTotal += scoredQuestions;
            if (catScore != null) {
                weightedSum += (catSum / catWeight) * category.effectiveWeight();
                weightTotal += category.effectiveWeight();
            }
        }

        if (!errors.isEmpty()) {
            throw new InvalidAnswersException(errors);
        }

        Integer overall = weightTotal > 0 ? (int) Math.round(weightedSum / weightTotal) : null;
        int completion = questionTotal == 0 ? 100 : (int) Math.round(answeredTotal * 100.0 / questionTotal);
        return new EvaluationResult(template.id(), template.version(), overall,
                overall == null ? null : status(template, overall),
                answeredTotal, questionTotal, completion, categoryResults);
    }

    /** Returns the 0–100 score for an answer, {@code null} for unscored or invalid answers. */
    private Double scoreQuestion(Question q, Object raw, List<AnswerError> errors) {
        return switch (q.type()) {
            case SCALE -> {
                Double v = asNumber(raw);
                if (v == null || v != Math.rint(v) || v < q.min() || v > q.max()) {
                    errors.add(new AnswerError(q.id(), "Choose a whole number from " + q.min() + " to " + q.max()));
                    yield null;
                }
                double s = (v - q.min()) * 100.0 / (q.max() - q.min());
                yield q.invert() ? 100 - s : s;
            }
            case YES_NO -> {
                if (!(raw instanceof Boolean b)) {
                    errors.add(new AnswerError(q.id(), "Answer yes or no"));
                    yield null;
                }
                yield b != q.invert() ? 100.0 : 0.0;
            }
            case CHOICE -> {
                String value = String.valueOf(raw);
                yield q.options().stream()
                        .filter(o -> o.value().equals(value))
                        .findFirst()
                        .map(Option::score)
                        .orElseGet(() -> {
                            errors.add(new AnswerError(q.id(), "Choose one of the listed options"));
                            return null;
                        });
            }
            case NUMBER -> {
                Double v = asNumber(raw);
                if (v == null || v.isNaN() || v.isInfinite()) {
                    errors.add(new AnswerError(q.id(), "Enter a number"));
                    yield null;
                }
                if ((q.min() != null && v < q.min()) || (q.max() != null && v > q.max())) {
                    errors.add(new AnswerError(q.id(), "Enter a value between "
                            + (q.min() == null ? "-∞" : q.min()) + " and " + (q.max() == null ? "∞" : q.max())));
                    yield null;
                }
                yield q.bands().stream()
                        .filter(b -> b.contains(v))
                        .findFirst()
                        .map(Band::score)
                        .orElse(0.0);
            }
            case TEXT -> {
                if (!(raw instanceof String s) || s.length() > MAX_TEXT_LENGTH) {
                    errors.add(new AnswerError(q.id(), "Text must be at most " + MAX_TEXT_LENGTH + " characters"));
                }
                yield null;
            }
        };
    }

    private static Status status(ScorecardTemplate template, int score) {
        Threshold t = template.thresholds().stream()
                .filter(th -> score >= th.min())
                .max(Comparator.comparingDouble(Threshold::min))
                .orElseThrow();
        return new Status(t.id(), t.label(), t.color(), t.summary());
    }

    private static Double asNumber(Object raw) {
        if (raw instanceof Number n) {
            return n.doubleValue();
        }
        if (raw instanceof String s) {
            try {
                return Double.parseDouble(s.trim());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private static boolean isEmpty(Object raw) {
        return raw == null || (raw instanceof String s && s.isBlank());
    }
}

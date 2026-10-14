package com.scorecardstudio.scoring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.scorecardstudio.TestTemplates;
import com.scorecardstudio.scoring.EvaluationResult.CategoryResult;
import com.scorecardstudio.template.ScorecardTemplate;

class ScoringEngineTest {

    private final ScoringEngine engine = new ScoringEngine();
    private final ScorecardTemplate template = TestTemplates.security();

    private static Map<String, Object> perfectAnswers() {
        Map<String, Object> a = new HashMap<>();
        a.put("mfa", true);
        a.put("shared-accounts", false);
        a.put("password-manager", 5);
        a.put("offboarding", "same-day");
        a.put("backup-frequency", "daily");
        a.put("restore-test-days", 10);
        a.put("encryption", true);
        a.put("auto-updates", 5);
        a.put("inventory", true);
        a.put("incident-plan", true);
        a.put("phishing-training", 5);
        a.put("notes", "All good");
        return a;
    }

    @Test
    void perfectAnswersScoreOneHundredEverywhere() {
        EvaluationResult r = engine.evaluate(template, perfectAnswers(), true);

        assertThat(r.overallScore()).isEqualTo(100);
        assertThat(r.status().id()).isEqualTo("protected");
        assertThat(r.completionPercent()).isEqualTo(100);
        assertThat(r.categories()).allSatisfy(c -> assertThat(c.score()).isEqualTo(100));
    }

    @Test
    void invertedYesNoAndWeightsAreApplied() {
        Map<String, Object> a = new HashMap<>();
        a.put("mfa", true);              // weight 2 -> 100
        a.put("shared-accounts", true);  // inverted -> 0
        a.put("password-manager", 3);    // (3-1)/(5-1) -> 50
        a.put("offboarding", "week");    // 60

        EvaluationResult r = engine.evaluate(template, a, false);
        CategoryResult identity = r.categories().get(0);

        // (100*2 + 0 + 50 + 60) / 5 = 62
        assertThat(identity.score()).isEqualTo(62);
        assertThat(identity.status().id()).isEqualTo("moderate");
        assertThat(identity.recommendation()).contains("Extend MFA");
    }

    @Test
    void numericBandsPickTheMatchingRange() {
        Map<String, Object> a = Map.of("restore-test-days", 120);
        EvaluationResult r = engine.evaluate(template, a, false);

        CategoryResult data = r.categories().get(1);
        assertThat(data.score()).isEqualTo(35);
        assertThat(data.answeredQuestions()).isEqualTo(1);
    }

    @Test
    void partialAnswersOnlyScoreAnsweredQuestionsAndReportCompletion() {
        EvaluationResult r = engine.evaluate(template, Map.of("mfa", true), false);

        assertThat(r.overallScore()).isEqualTo(100);
        assertThat(r.answeredQuestions()).isEqualTo(1);
        assertThat(r.totalQuestions()).isEqualTo(11);
        assertThat(r.categories().get(1).score()).isNull();
    }

    @Test
    void emptyAnswersHaveNoScore() {
        EvaluationResult r = engine.evaluate(template, Map.of(), false);

        assertThat(r.overallScore()).isNull();
        assertThat(r.status()).isNull();
        assertThat(r.completionPercent()).isZero();
    }

    @Test
    void submittingRequiresRequiredQuestions() {
        Map<String, Object> a = perfectAnswers();
        a.remove("mfa");
        a.remove("inventory"); // optional

        assertThatThrownBy(() -> engine.evaluate(template, a, true))
                .isInstanceOfSatisfying(InvalidAnswersException.class, e ->
                        assertThat(e.errors()).extracting(AnswerError::questionId).containsExactly("mfa"));
    }

    @Test
    void rejectsOutOfRangeWrongTypeAndUnknownAnswers() {
        Map<String, Object> a = new HashMap<>();
        a.put("password-manager", 9);
        a.put("password-manager-typo", 3);
        a.put("mfa", "yes");
        a.put("offboarding", "tomorrow");
        a.put("restore-test-days", -5);
        a.put("notes", "x".repeat(ScoringEngine.MAX_TEXT_LENGTH + 1));

        assertThatThrownBy(() -> engine.evaluate(template, a, false))
                .isInstanceOfSatisfying(InvalidAnswersException.class, e ->
                        assertThat(e.errors()).extracting(AnswerError::questionId).containsExactlyInAnyOrder(
                                "password-manager-typo", "mfa", "password-manager", "offboarding",
                                "restore-test-days", "notes"));
    }

    @Test
    void categoryWeightsShapeTheOverallScore() {
        Map<String, Object> a = new HashMap<>();
        a.put("mfa", true);           // identity (weight 1.5) -> 100
        a.put("incident-plan", false); // incident (weight 1) -> 0

        EvaluationResult r = engine.evaluate(template, a, false);

        // 100*1.5 / 2.5 = 60
        assertThat(r.overallScore()).isEqualTo(60);
    }
}

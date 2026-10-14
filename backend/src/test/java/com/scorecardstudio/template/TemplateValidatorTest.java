package com.scorecardstudio.template;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.scorecardstudio.TestTemplates;
import com.scorecardstudio.template.ScorecardTemplate.Branding;
import com.scorecardstudio.template.ScorecardTemplate.Category;
import com.scorecardstudio.template.ScorecardTemplate.Question;

class TemplateValidatorTest {

    @Test
    void bundledTemplatesAreValid() {
        assertThat(TemplateValidator.validate(TestTemplates.load("security-readiness"))).isEmpty();
        assertThat(TemplateValidator.validate(TestTemplates.load("launch-readiness"))).isEmpty();
    }

    @Test
    void rejectsColorsThatCouldInjectCss() {
        ScorecardTemplate t = TestTemplates.security();
        Branding evil = new Branding("Org", "Tag", "Logo", "red; background:url(http://evil)", "#000000");
        ScorecardTemplate broken = new ScorecardTemplate(t.id(), t.version(), t.name(), t.description(), evil,
                t.categories(), t.thresholds(), t.report());

        assertThat(TemplateValidator.validate(broken)).anyMatch(e -> e.startsWith("branding.primaryColor"));
    }

    @Test
    void rejectsDuplicateQuestionIdsAndBrokenScales() {
        ScorecardTemplate t = TestTemplates.security();
        Question badScale = new Question("mfa", "Duplicate id and inverted range", null, QuestionType.SCALE,
                true, null, 5, 1, null, null, false, null, null, null);
        Category extra = new Category("extra", "Extra", null, 1.0, List.of(badScale), null);
        List<Category> categories = new java.util.ArrayList<>(t.categories());
        categories.add(extra);
        ScorecardTemplate broken = new ScorecardTemplate(t.id(), t.version(), t.name(), t.description(),
                t.branding(), categories, t.thresholds(), t.report());

        List<String> errors = TemplateValidator.validate(broken);

        assertThat(errors).anyMatch(e -> e.contains("question id 'mfa' must be unique"));
        assertThat(errors).anyMatch(e -> e.contains("SCALE needs min < max"));
    }
}

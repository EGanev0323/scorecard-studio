package com.scorecardstudio.template;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import com.scorecardstudio.template.ScorecardTemplate.Category;
import com.scorecardstudio.template.ScorecardTemplate.Question;
import com.scorecardstudio.template.ScorecardTemplate.Threshold;

/**
 * Checks a template before it is accepted, so a broken config fails at startup with a clear
 * message instead of producing wrong scores later. Values that end up inside the PDF stylesheet
 * (colors) are restricted to strict hex codes to rule out CSS injection through config.
 */
final class TemplateValidator {

    private static final Pattern ID = Pattern.compile("[a-z0-9][a-z0-9-]{0,63}");
    private static final Pattern HEX_COLOR = Pattern.compile("#[0-9a-fA-F]{6}");

    private TemplateValidator() {
    }

    static List<String> validate(ScorecardTemplate t) {
        List<String> errors = new ArrayList<>();
        if (t.id() == null || !ID.matcher(t.id()).matches()) {
            errors.add("id must be lowercase letters, digits and dashes");
        }
        if (isBlank(t.name())) {
            errors.add("name is required");
        }
        if (t.branding() == null) {
            errors.add("branding is required");
        } else {
            checkColor(errors, "branding.primaryColor", t.branding().primaryColor());
            checkColor(errors, "branding.accentColor", t.branding().accentColor());
        }
        if (t.report() == null || isBlank(t.report().title())) {
            errors.add("report.title is required");
        }
        checkThresholds(errors, t.thresholds());
        checkCategories(errors, t);
        return errors;
    }

    private static void checkThresholds(List<String> errors, List<Threshold> thresholds) {
        if (thresholds == null || thresholds.isEmpty()) {
            errors.add("at least one threshold is required");
            return;
        }
        Set<String> ids = new HashSet<>();
        boolean coversZero = false;
        for (Threshold th : thresholds) {
            if (th.id() == null || !ids.add(th.id())) {
                errors.add("threshold ids must be present and unique");
            }
            if (th.min() < 0 || th.min() > 100) {
                errors.add("threshold '" + th.id() + "' min must be between 0 and 100");
            }
            checkColor(errors, "threshold '" + th.id() + "' color", th.color());
            coversZero |= th.min() == 0;
        }
        if (!coversZero) {
            errors.add("one threshold must start at 0 so every score has a status");
        }
    }

    private static void checkCategories(List<String> errors, ScorecardTemplate t) {
        if (t.categories() == null || t.categories().isEmpty()) {
            errors.add("at least one category is required");
            return;
        }
        Set<String> categoryIds = new HashSet<>();
        Set<String> questionIds = new HashSet<>();
        Set<String> thresholdIds = new HashSet<>();
        if (t.thresholds() != null) {
            t.thresholds().forEach(th -> thresholdIds.add(th.id()));
        }
        for (Category c : t.categories()) {
            String where = "category '" + c.id() + "'";
            if (c.id() == null || !ID.matcher(c.id()).matches() || !categoryIds.add(c.id())) {
                errors.add(where + ": id must be unique, lowercase letters, digits and dashes");
            }
            if (c.effectiveWeight() <= 0) {
                errors.add(where + ": weight must be positive");
            }
            if (c.recommendations() != null) {
                c.recommendations().keySet().stream()
                        .filter(k -> !thresholdIds.contains(k))
                        .forEach(k -> errors.add(where + ": recommendation key '" + k + "' is not a threshold id"));
            }
            if (c.questions() == null || c.questions().isEmpty()) {
                errors.add(where + ": at least one question is required");
                continue;
            }
            if (c.questions().stream().noneMatch(Question::scored)) {
                errors.add(where + ": needs at least one scored question");
            }
            for (Question q : c.questions()) {
                if (q.id() == null || !ID.matcher(q.id()).matches() || !questionIds.add(q.id())) {
                    errors.add(where + ": question id '" + q.id() + "' must be unique across the template");
                }
                checkQuestion(errors, q);
            }
        }
    }

    private static void checkQuestion(List<String> errors, Question q) {
        String where = "question '" + q.id() + "'";
        if (isBlank(q.label())) {
            errors.add(where + ": label is required");
        }
        if (q.type() == null) {
            errors.add(where + ": type is required");
            return;
        }
        if (q.effectiveWeight() <= 0) {
            errors.add(where + ": weight must be positive");
        }
        switch (q.type()) {
            case SCALE -> {
                if (q.min() == null || q.max() == null || q.min() >= q.max()) {
                    errors.add(where + ": SCALE needs min < max");
                }
            }
            case CHOICE -> {
                if (q.options() == null || q.options().size() < 2) {
                    errors.add(where + ": CHOICE needs at least two options");
                } else {
                    Set<String> values = new HashSet<>();
                    q.options().forEach(o -> {
                        if (o.value() == null || !values.add(o.value())) {
                            errors.add(where + ": option values must be present and unique");
                        }
                        if (o.score() < 0 || o.score() > 100) {
                            errors.add(where + ": option scores must be between 0 and 100");
                        }
                    });
                }
            }
            case NUMBER -> {
                if (q.bands() == null || q.bands().isEmpty()) {
                    errors.add(where + ": NUMBER needs scoring bands");
                } else if (q.bands().stream().anyMatch(b -> b.score() < 0 || b.score() > 100)) {
                    errors.add(where + ": band scores must be between 0 and 100");
                }
            }
            case YES_NO, TEXT -> {
            }
        }
    }

    private static void checkColor(List<String> errors, String field, String value) {
        if (value == null || !HEX_COLOR.matcher(value).matches()) {
            errors.add(field + " must be a hex color like #1f6feb");
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}

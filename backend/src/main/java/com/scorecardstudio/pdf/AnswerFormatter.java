package com.scorecardstudio.pdf;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.scorecardstudio.template.ScorecardTemplate;
import com.scorecardstudio.template.ScorecardTemplate.Category;
import com.scorecardstudio.template.ScorecardTemplate.Option;
import com.scorecardstudio.template.ScorecardTemplate.Question;

/** Turns raw stored answers into readable report rows, in template order. */
final class AnswerFormatter {

    record Row(String category, String question, String answer) {
    }

    private AnswerFormatter() {
    }

    static List<Row> rows(ScorecardTemplate template, Map<String, Object> answers) {
        List<Row> rows = new ArrayList<>();
        for (Category c : template.categories()) {
            for (Question q : c.questions()) {
                Object raw = answers.get(q.id());
                if (raw == null || (raw instanceof String s && s.isBlank())) {
                    continue;
                }
                rows.add(new Row(c.name(), q.label(), format(q, raw)));
            }
        }
        return rows;
    }

    private static String format(Question q, Object raw) {
        return switch (q.type()) {
            case YES_NO -> Boolean.TRUE.equals(raw) ? "Yes" : "No";
            case CHOICE -> q.options().stream()
                    .filter(o -> o.value().equals(String.valueOf(raw)))
                    .map(Option::label)
                    .findFirst()
                    .orElse(String.valueOf(raw));
            case SCALE -> number(raw) + " / " + q.max();
            case NUMBER -> number(raw) + (q.unit() == null ? "" : " " + q.unit());
            case TEXT -> String.valueOf(raw);
        };
    }

    private static String number(Object raw) {
        return new BigDecimal(String.valueOf(raw)).stripTrailingZeros().toPlainString();
    }
}

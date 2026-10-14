package com.scorecardstudio.pdf;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.Map;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import com.scorecardstudio.TestTemplates;
import com.scorecardstudio.scoring.EvaluationResult;
import com.scorecardstudio.scoring.ScoringEngine;
import com.scorecardstudio.submission.Submission;
import com.scorecardstudio.template.ScorecardTemplate;

class PdfReportRendererTest {

    private static SpringTemplateEngine templateEngine() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(resolver);
        return engine;
    }

    @Test
    void rendersOnePageWithEscapedUserInput() throws IOException {
        ScorecardTemplate template = TestTemplates.security();
        Map<String, Object> answers = Map.of("mfa", true, "password-manager", 4, "incident-plan", false);
        EvaluationResult result = new ScoringEngine().evaluate(template, answers, false);
        Submission submission = new Submission(template, "Иван <img src=x> & Co", "Acme \"Ltd\"", answers, result);

        byte[] pdf = new PdfReportRenderer(templateEngine()).render(submission);

        assertThat(new String(pdf, 0, 5)).isEqualTo("%PDF-");
        try (PDDocument doc = PDDocument.load(pdf)) {
            assertThat(doc.getNumberOfPages()).isEqualTo(1);
            String text = new PDFTextStripper().getText(doc);
            assertThat(text).contains("Security Readiness Scorecard", "Иван <img src=x> & Co", "Accounts & Access");
        }
    }
}

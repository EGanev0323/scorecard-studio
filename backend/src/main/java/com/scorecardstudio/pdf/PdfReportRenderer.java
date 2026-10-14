package com.scorecardstudio.pdf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder.FontStyle;
import com.scorecardstudio.submission.Submission;

/**
 * Renders a stored scorecard to a one-page PDF: Thymeleaf builds the HTML, OpenHTMLtoPDF lays it out.
 * All user input reaches the HTML through escaped Thymeleaf expressions, and the renderer is not
 * allowed to resolve any URI, so report content can never make the server fetch files or URLs.
 */
@Component
public class PdfReportRenderer {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH);

    private final TemplateEngine templateEngine;

    public PdfReportRenderer(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public byte[] render(Submission submission) {
        Context ctx = new Context(Locale.ENGLISH);
        ctx.setVariable("t", submission.getTemplateSnapshot());
        ctx.setVariable("s", submission);
        ctx.setVariable("r", submission.getResult());
        ctx.setVariable("answers", AnswerFormatter.rows(submission.getTemplateSnapshot(), submission.getAnswers()));
        ctx.setVariable("date", DATE.format(submission.getCreatedAt().atZone(ZoneId.of("UTC"))));
        String html = templateEngine.process("report/scorecard", ctx);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.useUriResolver((baseUri, uri) -> null);
            builder.useFont(() -> getClass().getResourceAsStream("/fonts/DejaVuSans.ttf"), "Report Sans", 400, FontStyle.NORMAL, true);
            builder.useFont(() -> getClass().getResourceAsStream("/fonts/DejaVuSans-Bold.ttf"), "Report Sans", 700, FontStyle.NORMAL, true);
            builder.withHtmlContent(html, null);
            builder.withProducer("Scorecard Studio");
            builder.toStream(out);
            builder.run();
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("PDF rendering failed", e);
        }
    }
}

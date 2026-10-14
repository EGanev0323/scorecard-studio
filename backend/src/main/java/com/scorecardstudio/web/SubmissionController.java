package com.scorecardstudio.web;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.scorecardstudio.pdf.PdfReportRenderer;
import com.scorecardstudio.submission.Submission;
import com.scorecardstudio.submission.SubmissionService;
import com.scorecardstudio.web.ApiDtos.SubmissionDetail;
import com.scorecardstudio.web.ApiDtos.SubmissionSummary;
import com.scorecardstudio.web.ApiDtos.SubmitRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
@Tag(name = "Scorecards", description = "Completed scorecards and their PDF reports")
public class SubmissionController {

    private final SubmissionService submissions;
    private final PdfReportRenderer pdf;

    public SubmissionController(SubmissionService submissions, PdfReportRenderer pdf) {
        this.submissions = submissions;
        this.pdf = pdf;
    }

    @PostMapping("/templates/{templateId}/submissions")
    @Operation(summary = "Validate, score and store a completed questionnaire")
    public ResponseEntity<SubmissionDetail> submit(@PathVariable String templateId,
                                                   @Valid @RequestBody SubmitRequest request) {
        Submission saved = submissions.submit(templateId, request.respondentName(), request.respondentOrg(), request.answers());
        return ResponseEntity.created(URI.create("/api/submissions/" + saved.getId()))
                .body(SubmissionDetail.of(saved));
    }

    @GetMapping("/submissions")
    @Operation(summary = "List recent scorecards, optionally for one template")
    public List<SubmissionSummary> recent(@RequestParam(required = false) String templateId,
                                          @RequestParam(defaultValue = "20") int limit) {
        return submissions.recent(templateId, limit).stream().map(SubmissionSummary::of).toList();
    }

    @GetMapping("/submissions/{id}")
    @Operation(summary = "Get a stored scorecard with its answers and result")
    public SubmissionDetail get(@PathVariable UUID id) {
        return SubmissionDetail.of(submissions.get(id));
    }

    @GetMapping(value = "/submissions/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Download the scorecard as a branded one-page PDF")
    public ResponseEntity<byte[]> pdf(@PathVariable UUID id) {
        Submission submission = submissions.get(id);
        String filename = submission.getTemplateId() + "-" + id.toString().substring(0, 8) + ".pdf";
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(filename).build().toString())
                .body(pdf.render(submission));
    }
}

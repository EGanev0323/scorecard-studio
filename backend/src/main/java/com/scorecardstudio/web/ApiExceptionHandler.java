package com.scorecardstudio.web;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.scorecardstudio.scoring.InvalidAnswersException;
import com.scorecardstudio.submission.SubmissionNotFoundException;
import com.scorecardstudio.template.TemplateNotFoundException;

/** Maps errors to RFC 7807 problem responses with a predictable shape and no internal details. */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler({TemplateNotFoundException.class, SubmissionNotFoundException.class})
    ProblemDetail notFound(RuntimeException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(InvalidAnswersException.class)
    ProblemDetail invalidAnswers(InvalidAnswersException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage());
        problem.setTitle("Invalid answers");
        problem.setProperty("errors", e.errors());
        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail invalidRequest(MethodArgumentNotValidException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Request failed validation");
        List<Map<String, String>> errors = e.getBindingResult().getFieldErrors().stream()
                .map(f -> Map.of("field", f.getField(), "message", String.valueOf(f.getDefaultMessage())))
                .toList();
        problem.setProperty("errors", errors);
        return problem;
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    ProblemDetail unreadable(Exception e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Malformed request");
    }
}

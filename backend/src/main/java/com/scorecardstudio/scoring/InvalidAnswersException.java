package com.scorecardstudio.scoring;

import java.util.List;

public class InvalidAnswersException extends RuntimeException {

    private final transient List<AnswerError> errors;

    public InvalidAnswersException(List<AnswerError> errors) {
        super("Answers failed validation (" + errors.size() + " problem(s))");
        this.errors = List.copyOf(errors);
    }

    public List<AnswerError> errors() {
        return errors;
    }
}

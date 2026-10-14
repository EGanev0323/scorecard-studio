package com.scorecardstudio.submission;

import java.util.UUID;

public class SubmissionNotFoundException extends RuntimeException {

    public SubmissionNotFoundException(UUID id) {
        super("Scorecard " + id + " not found");
    }
}

package com.scorecardstudio.template;

public class TemplateNotFoundException extends RuntimeException {

    public TemplateNotFoundException(String id) {
        super("Scorecard template '" + id + "' not found");
    }
}

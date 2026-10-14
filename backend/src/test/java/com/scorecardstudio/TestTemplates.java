package com.scorecardstudio;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scorecardstudio.template.ScorecardTemplate;

public final class TestTemplates {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private TestTemplates() {
    }

    public static ScorecardTemplate load(String id) {
        try (InputStream in = TestTemplates.class.getResourceAsStream("/scorecards/" + id + ".json")) {
            return MAPPER.readValue(in, ScorecardTemplate.class);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static ScorecardTemplate security() {
        return load("security-readiness");
    }
}

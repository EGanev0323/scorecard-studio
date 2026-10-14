package com.scorecardstudio.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Where scorecard templates are loaded from. Each entry is a Spring resource pattern,
 * e.g. {@code classpath:scorecards/*.json} or {@code file:/config/scorecards/*.json}.
 * Templates found later in the list override earlier ones with the same id.
 */
@ConfigurationProperties(prefix = "scorecard")
public record ScorecardProperties(List<String> templateLocations) {

    public ScorecardProperties {
        if (templateLocations == null || templateLocations.isEmpty()) {
            templateLocations = List.of("classpath:scorecards/*.json");
        }
    }
}

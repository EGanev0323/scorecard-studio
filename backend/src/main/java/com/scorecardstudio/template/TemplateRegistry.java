package com.scorecardstudio.template;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scorecardstudio.config.ScorecardProperties;

/**
 * Loads and validates scorecard templates from the configured locations at startup.
 * An invalid template stops the application with a list of everything that is wrong with it.
 */
@Component
public class TemplateRegistry {

    private static final Logger log = LoggerFactory.getLogger(TemplateRegistry.class);

    private final Map<String, ScorecardTemplate> templates;

    public TemplateRegistry(ScorecardProperties properties, ObjectMapper objectMapper) throws IOException {
        ObjectMapper strict = objectMapper.copy().enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Map<String, ScorecardTemplate> loaded = new LinkedHashMap<>();
        for (String location : properties.templateLocations()) {
            for (Resource resource : resolver.getResources(location)) {
                ScorecardTemplate template = read(strict, resource);
                loaded.put(template.id(), template);
                log.info("Loaded scorecard template '{}' v{} from {}", template.id(), template.version(), resource.getDescription());
            }
        }
        if (loaded.isEmpty()) {
            throw new IllegalStateException("No scorecard templates found in " + properties.templateLocations());
        }
        this.templates = Map.copyOf(loaded);
    }

    private static ScorecardTemplate read(ObjectMapper mapper, Resource resource) throws IOException {
        ScorecardTemplate template;
        try (InputStream in = resource.getInputStream()) {
            template = mapper.readValue(in, ScorecardTemplate.class);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot parse scorecard template " + resource.getDescription() + ": " + e.getMessage(), e);
        }
        List<String> errors = TemplateValidator.validate(template);
        if (!errors.isEmpty()) {
            throw new IllegalStateException("Invalid scorecard template " + resource.getDescription()
                    + ":\n - " + String.join("\n - ", errors));
        }
        return template;
    }

    public Collection<ScorecardTemplate> all() {
        return templates.values().stream()
                .sorted((a, b) -> a.name().compareToIgnoreCase(b.name()))
                .toList();
    }

    public ScorecardTemplate get(String id) {
        ScorecardTemplate template = templates.get(id);
        if (template == null) {
            throw new TemplateNotFoundException(id);
        }
        return template;
    }
}

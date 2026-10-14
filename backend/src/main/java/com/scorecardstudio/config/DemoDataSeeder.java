package com.scorecardstudio.config;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.scorecardstudio.submission.SubmissionRepository;
import com.scorecardstudio.submission.SubmissionService;

/** Adds a few example scorecards on first start so the demo is not empty. Disable with SEED_DEMO_DATA=false. */
@Component
@ConditionalOnProperty(name = "scorecard.seed-demo-data", havingValue = "true")
public class DemoDataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataSeeder.class);

    private final SubmissionService submissions;
    private final SubmissionRepository repository;

    public DemoDataSeeder(SubmissionService submissions, SubmissionRepository repository) {
        this.submissions = submissions;
        this.repository = repository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (repository.count() > 0) {
            return;
        }
        submissions.submit("security-readiness", "Maria Petrova", "Petrova Design Studio", Map.ofEntries(
                Map.entry("mfa", true), Map.entry("shared-accounts", true), Map.entry("password-manager", 3),
                Map.entry("offboarding", "week"), Map.entry("backup-frequency", "weekly"),
                Map.entry("restore-test-days", 120), Map.entry("encryption", true), Map.entry("auto-updates", 4),
                Map.entry("inventory", false), Map.entry("incident-plan", false), Map.entry("phishing-training", 2)));
        submissions.submit("security-readiness", "Daniel Novak", "Novak Accounting", Map.ofEntries(
                Map.entry("mfa", true), Map.entry("shared-accounts", false), Map.entry("password-manager", 5),
                Map.entry("offboarding", "same-day"), Map.entry("backup-frequency", "daily"),
                Map.entry("restore-test-days", 20), Map.entry("encryption", true), Map.entry("auto-updates", 5),
                Map.entry("inventory", true), Map.entry("incident-plan", true), Map.entry("phishing-training", 4),
                Map.entry("notes", "Annual review with external IT partner in March.")));
        submissions.submit("launch-readiness", "Alex Morgan", "Tasklane", Map.ofEntries(
                Map.entry("core-flows", 85), Map.entry("beta-users", true), Map.entry("open-bugs", 1),
                Map.entry("load-time", 1.8), Map.entry("load-tested", false), Map.entry("deployment", "scripted"),
                Map.entry("monitoring", 2), Map.entry("db-backups", true), Map.entry("https", true),
                Map.entry("privacy-policy", true), Map.entry("security-review", 3)));
        log.info("Seeded demo scorecards");
    }
}

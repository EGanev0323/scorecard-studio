CREATE TABLE submission (
    id                UUID PRIMARY KEY,
    template_id       VARCHAR(64)  NOT NULL,
    template_version  VARCHAR(32),
    template_name     VARCHAR(200) NOT NULL,
    respondent_name   VARCHAR(120) NOT NULL,
    respondent_org    VARCHAR(160),
    overall_score     INTEGER,
    status_label      VARCHAR(80),
    answers           JSONB        NOT NULL,
    result            JSONB        NOT NULL,
    template_snapshot JSONB        NOT NULL,
    created_at        TIMESTAMPTZ  NOT NULL
);

CREATE INDEX idx_submission_template_created ON submission (template_id, created_at DESC);

package com.scorecardstudio.template;

public enum QuestionType {
    /** Integer rating between {@code min} and {@code max}, scored linearly. */
    SCALE,
    /** Yes / no answer. Yes scores 100 unless {@code invert} is set. */
    YES_NO,
    /** Single choice from {@code options}; each option carries its own score (0–100). */
    CHOICE,
    /** Free numeric input, scored through {@code bands}. */
    NUMBER,
    /** Free text. Collected for the report but never scored. */
    TEXT
}

package com.scorecardstudio.submission;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubmissionRepository extends JpaRepository<Submission, UUID> {

    List<Submission> findAllByOrderByCreatedAtDesc(Pageable pageable);

    List<Submission> findByTemplateIdOrderByCreatedAtDesc(String templateId, Pageable pageable);
}

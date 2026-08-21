package com.hackathon.backend.repository;

import com.hackathon.backend.entity.IngestionJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IngestionJobRepository extends JpaRepository<IngestionJob, String> {
    Optional<IngestionJob> findTopByDocumentIdOrderByCreatedAtDesc(String documentId);
}

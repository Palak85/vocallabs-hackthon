package com.hackathon.backend.repository;

import com.hackathon.backend.entity.CitationAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CitationAnalyticsRepository extends JpaRepository<CitationAnalytics, String> {
    List<CitationAnalytics> findByMessageId(String messageId);
}

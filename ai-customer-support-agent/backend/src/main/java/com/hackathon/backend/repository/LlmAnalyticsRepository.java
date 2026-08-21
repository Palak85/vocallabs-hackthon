package com.hackathon.backend.repository;

import com.hackathon.backend.entity.LlmAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LlmAnalyticsRepository extends JpaRepository<LlmAnalytics, String> {
    Optional<LlmAnalytics> findByMessageId(String messageId);
}

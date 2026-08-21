package com.hackathon.backend.repository;

import com.hackathon.backend.entity.NlpAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NlpAnalyticsRepository extends JpaRepository<NlpAnalytics, String> {
    Optional<NlpAnalytics> findByMessageId(String messageId);
}

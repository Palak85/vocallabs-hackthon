package com.hackathon.backend.repository;

import com.hackathon.backend.entity.RetrievalAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RetrievalAnalyticsRepository extends JpaRepository<RetrievalAnalytics, String> {
    List<RetrievalAnalytics> findByMessageId(String messageId);
}

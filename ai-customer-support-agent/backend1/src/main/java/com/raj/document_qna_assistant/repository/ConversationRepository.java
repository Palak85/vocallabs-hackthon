package com.raj.document_qna_assistant.repository;

import com.raj.document_qna_assistant.entity.Conversation;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ConversationRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final ConversationRowMapper rowMapper = new ConversationRowMapper();

    public ConversationRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(Conversation conv) {
        String sql = """
            INSERT INTO conversations (
                id, tenant_id, title, mode, assigned_agent, escalation_status, escalation_reason,
                last_frustration_score, last_frustration_level, last_sentiment, last_emotion,
                last_intent, last_domain, call_status, created_at, updated_at
            )
            VALUES (
                :id, :tenantId, :title, :mode, :assignedAgent, :escalationStatus, :escalationReason,
                :lastFrustrationScore, :lastFrustrationLevel, :lastSentiment, :lastEmotion,
                :lastIntent, :lastDomain, :callStatus, :createdAt, :updatedAt
            )
            ON CONFLICT (id) DO UPDATE SET 
                title = :title,
                mode = :mode,
                assigned_agent = :assignedAgent,
                escalation_status = :escalationStatus,
                escalation_reason = :escalationReason,
                last_frustration_score = :lastFrustrationScore,
                last_frustration_level = :lastFrustrationLevel,
                last_sentiment = :lastSentiment,
                last_emotion = :lastEmotion,
                last_intent = :lastIntent,
                last_domain = :lastDomain,
                call_status = :callStatus,
                updated_at = :updatedAt
            """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", conv.getId().toString())
                .addValue("tenantId", conv.getTenantId())
                .addValue("title", conv.getTitle())
                .addValue("mode", conv.getMode())
                .addValue("assignedAgent", conv.getAssignedAgent())
                .addValue("escalationStatus", conv.getEscalationStatus())
                .addValue("escalationReason", conv.getEscalationReason())
                .addValue("lastFrustrationScore", conv.getLastFrustrationScore())
                .addValue("lastFrustrationLevel", conv.getLastFrustrationLevel())
                .addValue("lastSentiment", conv.getLastSentiment())
                .addValue("lastEmotion", conv.getLastEmotion())
                .addValue("lastIntent", conv.getLastIntent())
                .addValue("lastDomain", conv.getLastDomain())
                .addValue("callStatus", conv.getCallStatus())
                .addValue("createdAt", Timestamp.from(conv.getCreatedAt() != null ? conv.getCreatedAt() : Instant.now()))
                .addValue("updatedAt", Timestamp.from(conv.getUpdatedAt() != null ? conv.getUpdatedAt() : Instant.now()));
        jdbcTemplate.update(sql, params);
    }

    public Optional<Conversation> findByIdAndTenantId(UUID id, String tenantId) {
        String sql = "SELECT * FROM conversations WHERE id::text = :id AND tenant_id = :tenantId";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id.toString())
                .addValue("tenantId", tenantId);
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, params, rowMapper));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<Conversation> findById(UUID id) {
        String sql = "SELECT * FROM conversations WHERE id::text = :id";
        MapSqlParameterSource params = new MapSqlParameterSource("id", id.toString());
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, params, rowMapper));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<Conversation> findAllByTenantId(String tenantId) {
        String sql = "SELECT * FROM conversations WHERE tenant_id = :tenantId ORDER BY updated_at DESC";
        MapSqlParameterSource params = new MapSqlParameterSource("tenantId", tenantId);
        return jdbcTemplate.query(sql, params, rowMapper);
    }

    public boolean updateModeAndAgent(UUID convId, String tenantId, String mode, String assignedAgent, String escalationStatus) {
        String sql = """
            UPDATE conversations
            SET mode = :mode,
                assigned_agent = :assignedAgent,
                escalation_status = :escalationStatus,
                updated_at = :updatedAt
            WHERE id::text = :id AND tenant_id = :tenantId
            """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", convId.toString())
                .addValue("tenantId", tenantId)
                .addValue("mode", mode)
                .addValue("assignedAgent", assignedAgent)
                .addValue("escalationStatus", escalationStatus)
                .addValue("updatedAt", Timestamp.from(Instant.now()));
        return jdbcTemplate.update(sql, params) > 0;
    }

    public boolean updateNlpAndEscalation(UUID convId, String tenantId, int frustrationScore, String frustrationLevel,
                                          String sentiment, String emotion, String intent, String domain,
                                          String escalationStatus, String escalationReason) {
        String sql = """
            UPDATE conversations
            SET last_frustration_score = :score,
                last_frustration_level = :level,
                last_sentiment = :sentiment,
                last_emotion = :emotion,
                last_intent = :intent,
                last_domain = :domain,
                escalation_status = :status,
                escalation_reason = :reason,
                updated_at = :updatedAt
            WHERE id::text = :id AND tenant_id = :tenantId
            """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", convId.toString())
                .addValue("tenantId", tenantId)
                .addValue("score", frustrationScore)
                .addValue("level", frustrationLevel)
                .addValue("sentiment", sentiment)
                .addValue("emotion", emotion)
                .addValue("intent", intent)
                .addValue("domain", domain)
                .addValue("status", escalationStatus)
                .addValue("reason", escalationReason)
                .addValue("updatedAt", Timestamp.from(Instant.now()));
        return jdbcTemplate.update(sql, params) > 0;
    }

    public int countByTenantIdAndMode(String tenantId, String mode) {
        String sql = "SELECT COUNT(*) FROM conversations WHERE tenant_id = :tenantId AND mode = :mode";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("tenantId", tenantId)
                .addValue("mode", mode);
        Integer count = jdbcTemplate.queryForObject(sql, params, Integer.class);
        return count != null ? count : 0;
    }

    public int countByTenantIdAndEscalationStatus(String tenantId, String escalationStatus) {
        String sql = "SELECT COUNT(*) FROM conversations WHERE tenant_id = :tenantId AND escalation_status = :status";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("tenantId", tenantId)
                .addValue("status", escalationStatus);
        Integer count = jdbcTemplate.queryForObject(sql, params, Integer.class);
        return count != null ? count : 0;
    }

    public double getAverageFrustrationScore(String tenantId) {
        String sql = "SELECT AVG(last_frustration_score) FROM conversations WHERE tenant_id = :tenantId AND last_frustration_score > 0";
        MapSqlParameterSource params = new MapSqlParameterSource("tenantId", tenantId);
        Double avg = jdbcTemplate.queryForObject(sql, params, Double.class);
        return avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0;
    }

    public boolean deleteByIdAndTenantId(UUID id, String tenantId) {
        String sql = "DELETE FROM conversations WHERE id = :id AND tenant_id = :tenantId";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("tenantId", tenantId);
        return jdbcTemplate.update(sql, params) > 0;
    }

    private static class ConversationRowMapper implements RowMapper<Conversation> {
        @Override
        public Conversation mapRow(ResultSet rs, int rowNum) throws SQLException {
            Conversation conv = new Conversation();
            conv.setId(com.raj.document_qna_assistant.util.UuidUtils.parseSafely(getString(rs, "id", null)));
            conv.setTenantId(getString(rs, "tenant_id", "default"));
            conv.setTitle(getString(rs, "title", "Support Session"));
            conv.setMode(getString(rs, "mode", "AI"));
            conv.setAssignedAgent(getString(rs, "assigned_agent", null));
            conv.setEscalationStatus(getString(rs, "escalation_status", "NONE"));
            conv.setEscalationReason(getString(rs, "escalation_reason", null));
            
            try {
                conv.setLastFrustrationScore(rs.getInt("last_frustration_score"));
            } catch (SQLException ignored) {
                conv.setLastFrustrationScore(0);
            }
            
            conv.setLastFrustrationLevel(getString(rs, "last_frustration_level", "low"));
            conv.setLastSentiment(getString(rs, "last_sentiment", "neutral"));
            conv.setLastEmotion(getString(rs, "last_emotion", "neutral"));
            conv.setLastIntent(getString(rs, "last_intent", null));
            conv.setLastDomain(getString(rs, "last_domain", null));
            conv.setCallStatus(getString(rs, "call_status", "ACTIVE"));

            Timestamp created = getTimestamp(rs, "created_at");
            conv.setCreatedAt(created != null ? created.toInstant() : Instant.now());

            Timestamp updated = getTimestamp(rs, "updated_at");
            conv.setUpdatedAt(updated != null ? updated.toInstant() : Instant.now());
            return conv;
        }

        private String getString(ResultSet rs, String col, String fallback) {
            try {
                String val = rs.getString(col);
                return (val != null && !val.isBlank()) ? val : fallback;
            } catch (SQLException e) {
                return fallback;
            }
        }

        private Timestamp getTimestamp(ResultSet rs, String col) {
            try {
                return rs.getTimestamp(col);
            } catch (SQLException e) {
                return null;
            }
        }
    }
}

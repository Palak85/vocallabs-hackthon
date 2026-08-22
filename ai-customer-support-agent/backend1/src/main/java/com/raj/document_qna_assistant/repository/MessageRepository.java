package com.raj.document_qna_assistant.repository;

import com.raj.document_qna_assistant.dto.SourceDto;
import com.raj.document_qna_assistant.entity.Message;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public class MessageRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final MessageRowMapper rowMapper = new MessageRowMapper();

    public MessageRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(Message msg) {
        String sql = """
            INSERT INTO messages (id, conversation_id, role, content, token_count, model, latency_ms, created_at)
            VALUES (:id, :conversationId, :role, :content, :tokenCount, :model, :latencyMs, :createdAt)
            """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", msg.getId().toString())
                .addValue("conversationId", msg.getConversationId().toString())
                .addValue("role", msg.getRole())
                .addValue("content", msg.getContent())
                .addValue("tokenCount", msg.getTokenCount())
                .addValue("model", msg.getModel())
                .addValue("latencyMs", msg.getLatencyMs())
                .addValue("createdAt", Timestamp.from(msg.getCreatedAt() != null ? msg.getCreatedAt() : Instant.now()));
        jdbcTemplate.update(sql, params);
    }

    public List<Message> findAllByConversationId(UUID conversationId) {
        String sql = "SELECT * FROM messages WHERE conversation_id::text = :conversationId ORDER BY created_at ASC";
        MapSqlParameterSource params = new MapSqlParameterSource("conversationId", conversationId.toString());
        return jdbcTemplate.query(sql, params, rowMapper);
    }

    public void saveSource(UUID messageId, UUID chunkId, double similarityScore) {
        String sql = """
            INSERT INTO message_sources (id, message_id, chunk_id, similarity_score)
            VALUES (:id::uuid, :messageId::uuid, :chunkId::uuid, :similarityScore)
            """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", UUID.randomUUID().toString())
                .addValue("messageId", messageId.toString())
                .addValue("chunkId", chunkId.toString())
                .addValue("similarityScore", similarityScore);
        jdbcTemplate.update(sql, params);
    }

    public List<SourceDto> findSourcesForMessage(UUID messageId) {
        String sql = """
            SELECT c.metadata->>'title' AS title, 
                   (c.metadata->>'page_number')::int AS page_number, 
                   s.similarity_score, 
                   c.content AS snippet
            FROM message_sources s
            JOIN document_chunks c ON s.chunk_id = c.id
            WHERE s.message_id::text = :messageId
            """;
        MapSqlParameterSource params = new MapSqlParameterSource("messageId", messageId.toString());
        return jdbcTemplate.query(sql, params, (rs, rowNum) -> {
            String title = rs.getString("title");
            Integer page = rs.getObject("page_number") != null ? rs.getInt("page_number") : null;
            double score = rs.getDouble("similarity_score");
            String snippet = rs.getString("snippet");
            return new SourceDto(title, page, score, snippet);
        });
    }

    private static class MessageRowMapper implements RowMapper<Message> {
        @Override
        public Message mapRow(ResultSet rs, int rowNum) throws SQLException {
            Message msg = new Message();
            msg.setId(com.raj.document_qna_assistant.util.UuidUtils.parseSafely(getString(rs, "id", null)));
            msg.setConversationId(com.raj.document_qna_assistant.util.UuidUtils.parseSafely(getString(rs, "conversation_id", null)));
            msg.setRole(getString(rs, "role", "user"));
            msg.setContent(getString(rs, "content", ""));
            
            try {
                msg.setTokenCount(rs.getInt("token_count"));
            } catch (SQLException ignored) {
                msg.setTokenCount(0);
            }

            msg.setModel(getString(rs, "model", null));

            try {
                long lat = rs.getLong("latency_ms");
                msg.setLatencyMs(rs.wasNull() ? null : lat);
            } catch (SQLException ignored) {
                msg.setLatencyMs(null);
            }

            try {
                Timestamp created = rs.getTimestamp("created_at");
                msg.setCreatedAt(created != null ? created.toInstant() : Instant.now());
            } catch (SQLException ignored) {
                msg.setCreatedAt(Instant.now());
            }
            return msg;
        }

        private String getString(ResultSet rs, String col, String fallback) {
            try {
                String val = rs.getString(col);
                return (val != null && !val.isBlank()) ? val : fallback;
            } catch (SQLException e) {
                return fallback;
            }
        }
    }
}

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
            INSERT INTO conversations (id, tenant_id, title, created_at, updated_at)
            VALUES (:id, :tenantId, :title, :createdAt, :updatedAt)
            ON CONFLICT (id) DO UPDATE SET title = :title, updated_at = :updatedAt
            """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", conv.getId())
                .addValue("tenantId", conv.getTenantId())
                .addValue("title", conv.getTitle())
                .addValue("createdAt", Timestamp.from(conv.getCreatedAt() != null ? conv.getCreatedAt() : Instant.now()))
                .addValue("updatedAt", Timestamp.from(conv.getUpdatedAt() != null ? conv.getUpdatedAt() : Instant.now()));
        jdbcTemplate.update(sql, params);
    }

    public Optional<Conversation> findByIdAndTenantId(UUID id, String tenantId) {
        String sql = "SELECT * FROM conversations WHERE id = :id AND tenant_id = :tenantId";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("tenantId", tenantId);
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
            conv.setId(UUID.fromString(rs.getString("id")));
            conv.setTenantId(rs.getString("tenant_id"));
            conv.setTitle(rs.getString("title"));
            conv.setCreatedAt(rs.getTimestamp("created_at").toInstant());
            conv.setUpdatedAt(rs.getTimestamp("updated_at").toInstant());
            return conv;
        }
    }
}

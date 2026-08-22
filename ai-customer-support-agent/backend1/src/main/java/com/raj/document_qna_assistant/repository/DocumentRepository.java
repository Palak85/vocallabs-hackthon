package com.raj.document_qna_assistant.repository;

import com.raj.document_qna_assistant.entity.Document;
import com.raj.document_qna_assistant.entity.DocumentStatus;
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
public class DocumentRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final DocumentRowMapper rowMapper = new DocumentRowMapper();

    public DocumentRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(Document doc) {
        String sql = """
            INSERT INTO documents (id, tenant_id, title, category, filename, content_type, size_bytes, status, error_message, content_hash, created_at, updated_at)
            VALUES (:id, :tenantId, :title, :category, :filename, :contentType, :sizeBytes, :status, :errorMessage, :contentHash, :createdAt, :updatedAt)
            ON CONFLICT (tenant_id, content_hash) DO NOTHING
            """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", doc.getId())
                .addValue("tenantId", doc.getTenantId())
                .addValue("title", doc.getTitle())
                .addValue("category", doc.getCategory())
                .addValue("filename", doc.getFilename())
                .addValue("contentType", doc.getContentType())
                .addValue("sizeBytes", doc.getSizeBytes())
                .addValue("status", doc.getStatus().name())
                .addValue("errorMessage", doc.getErrorMessage())
                .addValue("contentHash", doc.getContentHash())
                .addValue("createdAt", Timestamp.from(doc.getCreatedAt() != null ? doc.getCreatedAt() : Instant.now()))
                .addValue("updatedAt", Timestamp.from(doc.getUpdatedAt() != null ? doc.getUpdatedAt() : Instant.now()));
        jdbcTemplate.update(sql, params);
    }

    public Optional<Document> findByIdAndTenantId(UUID id, String tenantId) {
        String sql = "SELECT * FROM documents WHERE id = :id AND tenant_id = :tenantId";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("tenantId", tenantId);
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, params, rowMapper));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<Document> findByTenantIdAndContentHash(String tenantId, String contentHash) {
        String sql = "SELECT * FROM documents WHERE tenant_id = :tenantId AND content_hash = :contentHash";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("tenantId", tenantId)
                .addValue("contentHash", contentHash);
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, params, rowMapper));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<Document> findAllByTenantId(String tenantId, int limit, int offset) {
        String sql = "SELECT * FROM documents WHERE tenant_id = :tenantId ORDER BY created_at DESC LIMIT :limit OFFSET :offset";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("tenantId", tenantId)
                .addValue("limit", limit)
                .addValue("offset", offset);
        return jdbcTemplate.query(sql, params, rowMapper);
    }

    public long countByTenantId(String tenantId) {
        String sql = "SELECT COUNT(*) FROM documents WHERE tenant_id = :tenantId";
        MapSqlParameterSource params = new MapSqlParameterSource("tenantId", tenantId);
        Long count = jdbcTemplate.queryForObject(sql, params, Long.class);
        return count != null ? count : 0L;
    }

    public boolean deleteByIdAndTenantId(UUID id, String tenantId) {
        String sql = "DELETE FROM documents WHERE id = :id AND tenant_id = :tenantId";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("tenantId", tenantId);
        return jdbcTemplate.update(sql, params) > 0;
    }

    public void deleteChunksByDocumentId(UUID documentId) {
        String sql = "DELETE FROM document_chunks WHERE document_id = :documentId";
        MapSqlParameterSource params = new MapSqlParameterSource("documentId", documentId);
        jdbcTemplate.update(sql, params);
    }

    public void updateStatus(UUID id, DocumentStatus status, String errorMessage) {
        String sql = "UPDATE documents SET status = :status, error_message = :errorMessage, updated_at = :updatedAt WHERE id = :id";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("status", status.name())
                .addValue("errorMessage", errorMessage)
                .addValue("updatedAt", Timestamp.from(Instant.now()));
        jdbcTemplate.update(sql, params);
    }

    private static class DocumentRowMapper implements RowMapper<Document> {
        @Override
        public Document mapRow(ResultSet rs, int rowNum) throws SQLException {
            Document doc = new Document();
            doc.setId(UUID.fromString(rs.getString("id")));
            doc.setTenantId(rs.getString("tenant_id"));
            doc.setTitle(rs.getString("title"));
            doc.setCategory(rs.getString("category"));
            doc.setFilename(rs.getString("filename"));
            doc.setContentType(rs.getString("content_type"));
            doc.setSizeBytes(rs.getLong("size_bytes"));
            doc.setStatus(DocumentStatus.valueOf(rs.getString("status")));
            doc.setErrorMessage(rs.getString("error_message"));
            doc.setContentHash(rs.getString("content_hash"));
            doc.setCreatedAt(rs.getTimestamp("created_at").toInstant());
            doc.setUpdatedAt(rs.getTimestamp("updated_at").toInstant());
            return doc;
        }
    }
}

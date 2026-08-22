package com.raj.document_qna_assistant.repository;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class TenantRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public TenantRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean existsById(String id) {
        String sql = "SELECT COUNT(*) FROM tenants WHERE id = :id";
        MapSqlParameterSource params = new MapSqlParameterSource("id", id);
        Integer count = jdbcTemplate.queryForObject(sql, params, Integer.class);
        return count != null && count > 0;
    }

    public void save(String id, String name) {
        String sql = "INSERT INTO tenants (id, name) VALUES (:id, :name) ON CONFLICT (id) DO NOTHING";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("name", name);
        jdbcTemplate.update(sql, params);
    }
}

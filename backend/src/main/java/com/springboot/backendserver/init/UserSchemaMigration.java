package com.springboot.backendserver.init;

import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * Aligns legacy {@code users} table with the current User entity when Hibernate ddl-auto
 * did not add new columns (common after entity changes on an existing database).
 */
@Component
@Order(0)
public class UserSchemaMigration {

    private final JdbcTemplate jdbcTemplate;

    public UserSchemaMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void migrate() {
        addColumnIfMissing("email", "VARCHAR(255) NULL");
        addColumnIfMissing("nickname", "VARCHAR(255) NULL");
        addColumnIfMissing("avatar", "VARCHAR(255) NULL");
        addColumnIfMissing("status", "VARCHAR(32) NOT NULL DEFAULT 'ACTIVE'");
        addColumnIfMissing("deleted", "TINYINT(1) NOT NULL DEFAULT 0");
        addColumnIfMissing("created_at", "DATETIME(6) NULL");

        jdbcTemplate.update("UPDATE users SET status = 'ACTIVE' WHERE status IS NULL OR status = ''");
        jdbcTemplate.update("UPDATE users SET deleted = 0 WHERE deleted IS NULL");
        jdbcTemplate.update("UPDATE users SET created_at = CURRENT_TIMESTAMP(6) WHERE created_at IS NULL");

        addUniqueIndexIfMissing("uk_users_email", "email");
    }

    private void addColumnIfMissing(String column, String definition) {
        if (columnExists(column)) {
            return;
        }
        jdbcTemplate.execute("ALTER TABLE users ADD COLUMN " + column + " " + definition);
    }

    private boolean columnExists(String column) {
        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*) FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = 'users'
                  AND COLUMN_NAME = ?
                """,
                Integer.class,
                column
        );
        return count != null && count > 0;
    }

    private void addUniqueIndexIfMissing(String indexName, String column) {
        if (!columnExists(column)) {
            return;
        }
        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*) FROM information_schema.STATISTICS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = 'users'
                  AND INDEX_NAME = ?
                """,
                Integer.class,
                indexName
        );
        if (count == null || count == 0) {
            try {
                jdbcTemplate.execute("ALTER TABLE users ADD UNIQUE INDEX " + indexName + " (" + column + ")");
            } catch (Exception ignored) {
                // duplicate values or index already present under another name
            }
        }
    }
}

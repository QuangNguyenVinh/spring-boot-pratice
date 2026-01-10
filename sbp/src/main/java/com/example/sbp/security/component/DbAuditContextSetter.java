package com.example.sbp.security.component;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class DbAuditContextSetter {
    private final JdbcTemplate jdbc;

    public DbAuditContextSetter(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void set(UUID userId, String username, UUID requestId, String ip, String userAgent) {

        jdbc.execute("""
            SET search_path TO auth;
            SELECT
              set_config('app.user_id',    '%s', true),
              set_config('app.username',   '%s', true),
              set_config('app.request_id', '%s', true),
              set_config('app.ip',         '%s', true),
              set_config('app.user_agent', '%s', true)
            """.formatted(
                        userId != null ? userId : "",
                        username != null ? username : "",
                        requestId != null ? requestId : "",
                        ip != null ? ip : "",
                        userAgent != null ? userAgent : ""
                )
        );
    }
}

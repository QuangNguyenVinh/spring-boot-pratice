package com.example.sbp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.net.InetAddress;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "audit_row", schema = "auth")
public class AuditRow {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @NotNull
    @ColumnDefault("now()")
    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "actor_user_id")
    private UUID actorUserId;

    @Column(name = "actor_username", length = Integer.MAX_VALUE)
    private String actorUsername;

    @Column(name = "request_id")
    private UUID requestId;

    @Column(name = "ip")
    private InetAddress ip;

    @Column(name = "user_agent", length = Integer.MAX_VALUE)
    private String userAgent;

    @NotNull
    @Column(name = "table_name", nullable = false, length = Integer.MAX_VALUE)
    private String tableName;

    @NotNull
    @Column(name = "operation", nullable = false, length = Integer.MAX_VALUE)
    private String operation;

    @Column(name = "row_pk")
    private UUID rowPk;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "old_data")
    private Map<String, Object> oldData;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "new_data")
    private Map<String, Object> newData;


}
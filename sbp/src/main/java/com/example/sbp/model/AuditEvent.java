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
@Table(name = "audit_event", schema = "auth")
public class AuditEvent {
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
    @Column(name = "action", nullable = false, length = Integer.MAX_VALUE)
    private String action;

    @Column(name = "target_type", length = Integer.MAX_VALUE)
    private String targetType;

    @Column(name = "target_id")
    private UUID targetId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "details")
    private Map<String, Object> details;


}
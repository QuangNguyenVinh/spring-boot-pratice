package com.example.sbp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "refresh_token", schema = "auth")
public class RefreshToken {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "session_id", nullable = false)
    private AuthSession session;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @NotNull
    @Column(name = "token_hash", nullable = false)
    private byte[] tokenHash;

    @NotNull
    @ColumnDefault("now()")
    @Column(name = "issued_at", nullable = false)
    private Instant issuedAt;

    @NotNull
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "replaced_by_token_id")
    private RefreshToken replacedByToken;

    @NotNull
    @ColumnDefault("false")
    @Column(name = "revoked", nullable = false)
    private Boolean revoked;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "revoke_reason", length = Integer.MAX_VALUE)
    private String revokeReason;

    @OneToMany(mappedBy = "replacedByToken")
    private Set<RefreshToken> refreshTokens = new LinkedHashSet<>();
}
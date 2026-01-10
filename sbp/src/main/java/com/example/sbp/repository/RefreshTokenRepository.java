package com.example.sbp.repository;

import com.example.sbp.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    @Query("""
                select rt from RefreshToken rt
                where rt.tokenHash = :hash
                  and rt.expiresAt > CURRENT_TIMESTAMP
            """)
    Optional<RefreshToken> findByHash(@Param("hash") byte[] hash);

    @Modifying
    @Query("""
                update RefreshToken rt
                set rt.revoked = true,
                    rt.revokedAt = CURRENT_TIMESTAMP,
                    rt.revokeReason = :reason,
                    rt.replacedByToken.id = :newId
                where rt.id = :oldId
                  and rt.revoked = false
            """)
    int rotate(@Param("oldId") UUID oldId,
               @Param("newId") UUID newId,
               @Param("reason") String reason);

    @Modifying
    @Query("""
                update RefreshToken rt
                set rt.revoked = true,
                    rt.revokedAt = CURRENT_TIMESTAMP,
                    rt.revokeReason = :reason
                where rt.session.id = :sessionId
                  and rt.revoked = false
            """)
    int revokeSession(@Param("sessionId") UUID sessionId,
                      @Param("reason") String reason);
}
package co.com.fcv.training.citas.adapter.persistence;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "refresh_sessions")
class SessionEntity {
    @Id @Column(length = 36, columnDefinition = "char(36)") String id;
    @Column(name = "user_id", nullable = false, length = 36, columnDefinition = "char(36)") String userId;
    @Column(name = "jti_hash", nullable = false, length = 64, columnDefinition = "char(64)") String jtiHash;
    @Column(name = "expires_at", nullable = false) Instant expiresAt;
    @Column(name = "revoked_at") Instant revokedAt;
    protected SessionEntity() {}
}

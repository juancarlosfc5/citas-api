package co.com.fcv.training.citas.domain;

import java.time.Instant;
import java.util.UUID;

public record RefreshSession(UUID id, UUID userId, String jtiHash, Instant expiresAt, Instant revokedAt) {
    public boolean activeAt(Instant now) {
        return revokedAt == null && expiresAt.isAfter(now);
    }
}

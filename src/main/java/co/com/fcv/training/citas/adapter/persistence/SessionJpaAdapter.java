package co.com.fcv.training.citas.adapter.persistence;

import co.com.fcv.training.citas.application.Ports;
import co.com.fcv.training.citas.domain.RefreshSession;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
class SessionJpaAdapter implements Ports.Sessions {
    private final SessionsJpa sessions;
    SessionJpaAdapter(SessionsJpa sessions) { this.sessions = sessions; }

    public void save(RefreshSession session) {
        SessionEntity e = new SessionEntity();
        e.id = session.id().toString();
        e.userId = session.userId().toString();
        e.jtiHash = session.jtiHash();
        e.expiresAt = session.expiresAt();
        e.revokedAt = session.revokedAt();
        sessions.saveAndFlush(e);
    }

    public Optional<RefreshSession> lockByJtiHash(String hash) {
        return sessions.lockByJtiHash(hash).map(e -> new RefreshSession(UUID.fromString(e.id),
                UUID.fromString(e.userId), e.jtiHash, e.expiresAt, e.revokedAt));
    }

    public void revoke(UUID id, Instant when) {
        SessionEntity e = sessions.findById(id.toString()).orElseThrow();
        e.revokedAt = when;
        sessions.saveAndFlush(e);
    }
}

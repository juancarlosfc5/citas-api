package co.com.fcv.training.citas.application;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** Local-only delivery substitute. Tokens remain in process memory and are never logged or persisted. */
@Component @Profile("local")
public class LocalPasswordResetMailbox {
    private record Entry(String token, Instant expiresAt) {}
    private final ConcurrentHashMap<String,Entry> entries=new ConcurrentHashMap<>(); private final Clock clock;
    public LocalPasswordResetMailbox(Clock clock){this.clock=clock;}
    public void put(String email,String token){entries.put(email.trim().toLowerCase(),new Entry(token,clock.instant().plusSeconds(1800)));}
    public Optional<String> take(String email){Entry e=entries.remove(email.trim().toLowerCase());return e!=null&&e.expiresAt().isAfter(clock.instant())?Optional.of(e.token()):Optional.empty();}
}

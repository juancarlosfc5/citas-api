package co.com.fcv.training.citas.application;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.HexFormat;

@Service
public class PasswordRecoveryService {
    private final JdbcTemplate jdbc; private final Ports.Passwords passwords; private final Clock clock; private final ObjectProvider<LocalPasswordResetMailbox> mailbox;
    public PasswordRecoveryService(JdbcTemplate jdbc, Ports.Passwords passwords, Clock clock, ObjectProvider<LocalPasswordResetMailbox> mailbox) { this.jdbc=jdbc; this.passwords=passwords; this.clock=clock; this.mailbox=mailbox; }
    @Transactional public void request(String email) {
        if(email==null) return; Long user=jdbc.query("select id from users where lower(email)=lower(?) and active=true",(r,n)->r.getLong(1),email.trim()).stream().findFirst().orElse(null);
        if(user==null)return; byte[] bytes=new byte[32];new SecureRandom().nextBytes(bytes);String raw=HexFormat.of().formatHex(bytes);
        jdbc.update("update password_reset_tokens set used_at=? where user_id=? and used_at is null",LocalDateTime.now(clock),user);
        jdbc.update("insert into password_reset_tokens(user_id,token_hash,expires_at) values (?,?,?)",user,AuthService.hash(raw),LocalDateTime.now(clock).plusMinutes(30));
        LocalPasswordResetMailbox local=mailbox.getIfAvailable(); if(local!=null)local.put(email,raw);
    }
    @Transactional public void reset(String token,String password) {
        if(token==null||token.isBlank()||password==null||password.isBlank()||password.length()>72) throw new IllegalArgumentException("Solicitud inválida");
        var row=jdbc.query("select id,user_id from password_reset_tokens where token_hash=? and used_at is null and expires_at>? for update",(r,n)->new long[]{r.getLong(1),r.getLong(2)},AuthService.hash(token),LocalDateTime.now(clock)).stream().findFirst().orElseThrow(AuthFailure::new);
        jdbc.update("update users set password_hash=? where id=?",passwords.hash(password),row[1]);
        jdbc.update("update password_reset_tokens set used_at=? where id=?",LocalDateTime.now(clock),row[0]);
        jdbc.update("update refresh_tokens set revoked_at=? where user_id=? and revoked_at is null",LocalDateTime.now(clock),row[1]);
    }
}

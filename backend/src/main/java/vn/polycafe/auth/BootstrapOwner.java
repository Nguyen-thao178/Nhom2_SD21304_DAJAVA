package vn.polycafe.auth;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Component
public class BootstrapOwner implements ApplicationRunner {
    private final JdbcTemplate db; private final PasswordEncoder passwords;
    private final TransactionTemplate transaction;
    @Value("${app.bootstrap.username:}") private String username;
    @Value("${app.bootstrap.password:}") private String password;
    @Value("${app.bootstrap.full-name:Chủ quán PolyCafe}") private String fullName;
    public BootstrapOwner(JdbcTemplate db,PasswordEncoder passwords,PlatformTransactionManager manager) {
        this.db=db;this.passwords=passwords;this.transaction=new TransactionTemplate(manager);
    }
    @Override public void run(ApplicationArguments args) {
        if(username.isBlank() || password.isBlank()) return;
        int bytes=password.getBytes(StandardCharsets.UTF_8).length;
        if(username.length()>60 || fullName.isBlank() || fullName.length()>150 || bytes<12 || bytes>72 || password.contains("CHANGE_TO")) throw new IllegalArgumentException("Bootstrap password must be a custom password, 12–72 UTF-8 bytes.");
        transaction.executeWithoutResult(status -> createOwnerIfAbsent());
    }
    private void createOwnerIfAbsent() {
        if(db.queryForObject("SELECT count(*) FROM app_user WHERE username=?",Integer.class,username)>0) return;
        UUID id=UUID.randomUUID();
        db.update("INSERT INTO app_user(id,username,full_name,password_hash,status,account_kind) VALUES(?,?,?,?,'active','staff')",id,username,fullName,passwords.encode(password));
        db.update("INSERT INTO user_access(user_id,role,permissions,status) VALUES(?,'owner',CAST(? AS jsonb),'active')",id,"{\"schema_version\":1}");
    }
}

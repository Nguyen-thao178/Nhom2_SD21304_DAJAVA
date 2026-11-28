package vn.polycafe.auth;

import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import vn.polycafe.common.*;

@Service
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final AuditService audit;
    private final String dummyHash;
    public AuthService(UserRepository users, PasswordEncoder encoder, AuditService audit) {
        this.users=users; this.encoder=encoder; this.audit=audit; this.dummyHash=encoder.encode(UUID.randomUUID().toString());
    }
    // Return null rather than throw here so failed_count is committed.
    @Transactional
    public StaffUser login(String name,String password) {
        if(password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length>72) return null;
        var account=users.byUsernameLocked(name);
        if(account.isEmpty()) { encoder.matches(password,dummyHash); return null; }
        var a=account.get();
        if(!users.allowed(a)) { encoder.matches(password,dummyHash); return null; }
        if(!encoder.matches(password,a.hash())) { users.failed(a); return null; }
        var staff=users.staff(a);
        if(staff.roles().stream().noneMatch(Set.of("staff","shift_lead","owner","sysadmin","accountant","customer")::contains)) return null;
        users.reset(a.id()); audit.record(a.id(),"login","app_user",a.id(),null,null); return staff;
    }
    @Transactional
    public void logout(StaffUser user) { users.revoke(user.id()); audit.record(user.id(),"logout","app_user",user.id(),null,null); }
}

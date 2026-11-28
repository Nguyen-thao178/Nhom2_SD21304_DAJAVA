package vn.polycafe.auth;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {
    private final JdbcTemplate db;
    public UserRepository(JdbcTemplate db) { this.db=db; }
    public record Account(UUID id,String username,String fullName,String hash,String status,String kind,int failed,int version,OffsetDateTime lockedUntil) {}
    private Account row(ResultSet r,int n) throws SQLException {
        return new Account(r.getObject("id",UUID.class),r.getString("username"),r.getString("full_name"),r.getString("password_hash"),r.getString("status"),r.getString("account_kind"),r.getInt("failed_count"),r.getInt("refresh_version"),r.getObject("locked_until",OffsetDateTime.class));
    }
    public Optional<Account> byUsernameLocked(String name) { return db.query("SELECT * FROM app_user WHERE username=? FOR UPDATE",this::row,name).stream().findFirst(); }
    public Optional<Account> byId(UUID id) { return db.query("SELECT * FROM app_user WHERE id=?",this::row,id).stream().findFirst(); }
    public StaffUser staff(Account a) {
        if(a.kind().equals("customer")) return new StaffUser(a.id(),a.username(),a.fullName(),a.version(),Set.of("customer"),false);
        var rows=db.queryForList("SELECT role,branch_id FROM user_access WHERE user_id=? AND status='active'",a.id());
        Set<String> roles=new HashSet<>(); boolean manage=false;
        for(var r:rows) {
            String role=(String)r.get("role"); roles.add(role);
            if(r.get("branch_id")==null && Set.of("owner","sysadmin").contains(role)) manage=true;
        }
        return new StaffUser(a.id(),a.username(),a.fullName(),a.version(),roles,manage);
    }
    public boolean allowed(Account a) {
        boolean kindAllowed=a.kind().equals("staff") || (a.kind().equals("customer") && db.queryForObject("SELECT count(*) FROM customer WHERE user_id=? AND kind='member' AND status='active'",Integer.class,a.id())==1);
        return kindAllowed && a.status().equals("active") && (a.lockedUntil()==null || !a.lockedUntil().isAfter(OffsetDateTime.now()));
    }
    public void failed(Account a) {
        int count=(a.lockedUntil()!=null && !a.lockedUntil().isAfter(OffsetDateTime.now()))?1:a.failed()+1;
        db.update("UPDATE app_user SET failed_count=?,locked_until=?,updated_at=now() WHERE id=?",count,count>=5?OffsetDateTime.now().plusMinutes(15):null,a.id());
    }
    public void reset(UUID id) { db.update("UPDATE app_user SET failed_count=0,locked_until=NULL,updated_at=now() WHERE id=?",id); }
    public void revoke(UUID id) { db.update("UPDATE app_user SET refresh_version=refresh_version+1,updated_at=now() WHERE id=?",id); }
}

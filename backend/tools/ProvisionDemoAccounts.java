import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.*;
import java.security.SecureRandom;
import java.util.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/** Explicit local provisioning command; never runs at application startup. */
class ProvisionDemoAccounts {
 public static void main(String[] args) throws Exception {
  var config=new Properties();
  try(var r=Files.newBufferedReader(Path.of("application-local.properties"),StandardCharsets.UTF_8)){config.load(r);}
  var credentials=new ArrayList<String>();
  DriverManager.setLoginTimeout(15);
  try(var c=DriverManager.getConnection(config.getProperty("spring.datasource.url"),config.getProperty("spring.datasource.username"),config.getProperty("spring.datasource.password"))){
   c.setAutoCommit(false);
   try {
    try(var s=c.prepareStatement("SELECT pg_advisory_xact_lock(hashtext(?))")){s.setString(1,"polycafe-demo-accounts-v1");s.execute();}
    UUID branch;
    try(var s=c.prepareStatement("SELECT id FROM branch WHERE code=? AND status='active'")){s.setString(1,"DEMO-PC01");try(var r=s.executeQuery()){if(!r.next())throw new IllegalStateException("Seed the demo branch first");branch=r.getObject(1,UUID.class);}}
    for(String username:List.of("nhanvien1","thanhvien1")){
     try(var s=c.prepareStatement("SELECT id FROM app_user WHERE username=?")){s.setString(1,username);try(var r=s.executeQuery()){if(r.next()){System.out.println(username+" already exists; unchanged.");continue;}}}
     boolean employee=username.equals("nhanvien1");UUID id=UUID.randomUUID();
     byte[] random=new byte[15];new SecureRandom().nextBytes(random);String password=Base64.getUrlEncoder().withoutPadding().encodeToString(random);
     String fullName=employee?"Nhân viên mẫu 1":"Thành viên mẫu 1";
     try(var s=c.prepareStatement("INSERT INTO app_user(id,username,full_name,password_hash,status,account_kind) VALUES(?,?,?,?,'active',?)")){s.setObject(1,id);s.setString(2,username);s.setString(3,fullName);s.setString(4,new BCryptPasswordEncoder(12).encode(password));s.setString(5,employee?"staff":"customer");s.executeUpdate();}
     try(var s=c.prepareStatement("INSERT INTO user_access(user_id,branch_id,role,permissions,status,discount_limit_pct,refund_limit) VALUES(?,?,?,CAST(? AS jsonb),'active',0,0)")){s.setObject(1,id);s.setObject(2,employee?branch:null);s.setString(3,employee?"staff":"customer");s.setString(4,"{\"schema_version\":1}");s.executeUpdate();}
     if(!employee){try(var s=c.prepareStatement("INSERT INTO customer(id,user_id,member_code,kind,full_name,email,tier,points_balance,status) VALUES(?,?,?,'member',?,?,'standard',0,'active')")){s.setObject(1,UUID.randomUUID());s.setObject(2,id);s.setString(3,"DEMO-MEMBER-LOGIN1");s.setString(4,fullName);s.setString(5,"demo.member.login1@example.invalid");s.executeUpdate();}}
     else {try(var s=c.prepareStatement("INSERT INTO shift_assignment(shift_id,user_id,from_at,duty,zone_code) SELECT id,?,CURRENT_TIMESTAMP,?,'A' FROM shift WHERE branch_id=? AND status='open' ORDER BY opened_at DESC LIMIT 1")){s.setObject(1,id);s.setString(2,"Phục vụ QR");s.setObject(3,branch);s.executeUpdate();}}
     credentials.add(username+" = "+password);
    }
    // Write before commit so a filesystem failure cannot leave inaccessible accounts.
    if(!credentials.isEmpty())Files.write(Path.of("demo-accounts.local.txt"),credentials,StandardCharsets.UTF_8,StandardOpenOption.CREATE,StandardOpenOption.APPEND);
    c.commit();System.out.println("Provisioned "+credentials.size()+" accounts. Credentials saved only in ignored demo-accounts.local.txt.");
   }catch(Exception e){c.rollback();throw e;}
  }
 }
}

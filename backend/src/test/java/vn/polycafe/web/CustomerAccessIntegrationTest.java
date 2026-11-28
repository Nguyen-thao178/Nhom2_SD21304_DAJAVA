package vn.polycafe.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.*;
import java.net.http.*;
import java.util.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT,properties={
 "server.servlet.context-path=/polycafe","spring.datasource.url=jdbc:h2:mem:customer_access;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
 "spring.datasource.username=sa","spring.datasource.password=","spring.datasource.driver-class-name=org.h2.Driver",
 "spring.datasource.hikari.connection-init-sql=","app.bootstrap.username=","app.bootstrap.password="})
class CustomerAccessIntegrationTest {
 @LocalServerPort int port;
 @Autowired JdbcTemplate db;
 @Autowired PasswordEncoder encoder;
 @Autowired ObjectMapper json;
 HttpClient client;String base;UUID member,other;
 @BeforeEach void setup(){
  base="http://localhost:"+port+"/polycafe";
  client=HttpClient.newBuilder().cookieHandler(new CookieManager(null,CookiePolicy.ACCEPT_ALL)).build();
  db.execute("CREATE TABLE IF NOT EXISTS app_user(id uuid primary key,username varchar(60),full_name varchar(150),password_hash varchar(255),status varchar(20),account_kind varchar(20),failed_count int,refresh_version int,locked_until timestamp with time zone,updated_at timestamp with time zone default now())");
  db.execute("CREATE TABLE IF NOT EXISTS customer(id uuid primary key,user_id uuid,member_code varchar(60),full_name varchar(150),tier varchar(20),points_balance int,kind varchar(20),status varchar(20))");
  db.execute("CREATE TABLE IF NOT EXISTS audit_log(id uuid default random_uuid() primary key,actor_id uuid,action varchar(50),entity_type varchar(40),entity_id uuid,before_data jsonb,after_data jsonb,request_id varchar(64))");
  db.execute("CREATE TABLE IF NOT EXISTS dining_table(id uuid primary key,code varchar(30))");
  db.execute("CREATE TABLE IF NOT EXISTS invoice(id uuid primary key,invoice_no varchar(50),status varchar(30),total numeric,paid_total numeric,created_at timestamp with time zone default now(),table_id uuid,customer_id uuid)");
  db.update("DELETE FROM invoice");db.update("DELETE FROM customer");db.update("DELETE FROM app_user");
  UUID user=UUID.randomUUID();member=UUID.randomUUID();other=UUID.randomUUID();
  db.update("INSERT INTO app_user VALUES(?,?,?,?,'active','customer',0,0,NULL,CURRENT_TIMESTAMP)",user,"member-test","Khách thành viên",encoder.encode("Customer-test-2026!"));
  db.update("INSERT INTO customer VALUES(?,?,? ,?,'standard',0,'member','active')",member,user,"MEM-1","Khách thành viên");
  db.update("INSERT INTO customer VALUES(?,NULL,'MEM-2','Khách khác','standard',0,'member','active')",other);
  db.update("INSERT INTO invoice(id,invoice_no,status,total,paid_total,customer_id) VALUES(?,'OWN-ORDER','draft',25000,0,?)",UUID.randomUUID(),member);
  db.update("INSERT INTO invoice(id,invoice_no,status,total,paid_total,customer_id) VALUES(?,'OTHER-PRIVATE-ORDER','paid',50000,50000,?)",UUID.randomUUID(),other);
 }
 HttpResponse<String> get(String path)throws Exception{return client.send(HttpRequest.newBuilder(URI.create(base+path)).GET().build(),HttpResponse.BodyHandlers.ofString());}
 void login()throws Exception{
  var csrf=json.readTree(get("/api/v1/auth/csrf").body());
  var r=client.send(HttpRequest.newBuilder(URI.create(base+"/api/v1/auth/login")).header("Content-Type","application/json").header(csrf.get("headerName").asText(),csrf.get("token").asText()).POST(HttpRequest.BodyPublishers.ofString("{\"username\":\"member-test\",\"password\":\"Customer-test-2026!\"}")).build(),HttpResponse.BodyHandlers.ofString());
  assertEquals(200,r.statusCode(),r.body());assertTrue(r.body().contains("customer"));
 }
 @Test void memberCanOnlyReadOwnProfileAndOrders()throws Exception{
  login();var profile=get("/api/customer/profile");assertEquals(200,profile.statusCode());assertTrue(profile.body().contains("MEM-1"));
  var orders=get("/api/customer/orders");assertEquals(200,orders.statusCode());assertTrue(orders.body().contains("OWN-ORDER"));assertFalse(orders.body().contains("OTHER-PRIVATE-ORDER"));
  for(String path:List.of("/app/overview","/app/qr-codes","/api/v1/app-user","/api/v1/customer","/api/staff/qr-tables","/api/staff/qr-orders","/api/v1/products","/api/ui/reports"))assertEquals(403,get(path).statusCode(),path);
  assertEquals(302,get("/login").statusCode());
 }
 @Test void customerPageIsPublicUtf8AndInactiveMemberLosesSession()throws Exception{
  var page=get("/customer");assertEquals(200,page.statusCode());assertTrue(page.body().contains("Quét mã QR tại bàn"));
  assertEquals(200,get("/qr/sample-token").statusCode());
  assertEquals(401,get("/api/customer/profile").statusCode());login();db.update("UPDATE customer SET status='anonymized' WHERE id=?",member);assertEquals(401,get("/api/customer/profile").statusCode());
 }
}

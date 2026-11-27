package vn.polycafe.web;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.*;
import java.net.http.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

/** Uses real Tomcat and Jasper so a successful MVC forward cannot hide a broken JSP. */
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT,properties={
    "server.servlet.context-path=/polycafe",
    "spring.datasource.url=jdbc:h2:mem:jsp;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
    "spring.datasource.username=sa","spring.datasource.password=",
    "spring.datasource.driver-class-name=org.h2.Driver","spring.datasource.hikari.connection-init-sql=",
    "app.bootstrap.username=","app.bootstrap.password="})
class JspPageIntegrationTest {
    @LocalServerPort int port;
    @Autowired JdbcTemplate db;
    @Autowired PasswordEncoder encoder;
    @Autowired ObjectMapper json;
    HttpClient client;
    String base;
    @BeforeEach void setup() {
        base="http://localhost:"+port+"/polycafe";
        client=HttpClient.newBuilder().cookieHandler(new CookieManager(null,CookiePolicy.ACCEPT_ALL)).build();
        db.execute("CREATE TABLE IF NOT EXISTS app_user(id uuid primary key,username varchar(60),full_name varchar(150),password_hash varchar(255),status varchar(20),account_kind varchar(20),failed_count int,refresh_version int,locked_until timestamp with time zone,updated_at timestamp with time zone default now())");
        db.execute("CREATE TABLE IF NOT EXISTS user_access(id uuid primary key,user_id uuid,role varchar(20),branch_id uuid,status varchar(20))");
        db.execute("CREATE TABLE IF NOT EXISTS audit_log(id uuid default random_uuid() primary key,actor_id uuid,action varchar(50),entity_type varchar(40),entity_id uuid,before_data jsonb,after_data jsonb,request_id varchar(64))");
        db.update("DELETE FROM user_access");db.update("DELETE FROM app_user");
        UUID id=UUID.randomUUID();
        db.update("INSERT INTO app_user(id,username,full_name,password_hash,status,account_kind,failed_count,refresh_version) VALUES(?,?,?,?,'active','staff',0,0)",id,"jsp-owner","<script>unsafe</script>",encoder.encode("Test-JSP-only-123!"));
        db.update("INSERT INTO user_access VALUES(?,?,'owner',NULL,'active')",UUID.randomUUID(),id);
    }
    HttpResponse<String> get(String path) throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create(base+path)).GET().build(),HttpResponse.BodyHandlers.ofString());
    }
    void login() throws Exception {
        var csrf=json.readTree(get("/api/v1/auth/csrf").body());
        var response=client.send(HttpRequest.newBuilder(URI.create(base+"/api/v1/auth/login"))
            .header("Content-Type","application/json").header(csrf.get("headerName").asText(),csrf.get("token").asText())
            .POST(HttpRequest.BodyPublishers.ofString("{\"username\":\"jsp-owner\",\"password\":\"Test-JSP-only-123!\"}")).build(),HttpResponse.BodyHandlers.ofString());
        assertEquals(200,response.statusCode(),response.body());
    }
    @Test void vueCorsPreflightAllowsConfiguredOrigin() throws Exception {
        var response=client.send(HttpRequest.newBuilder(URI.create(base+"/api/v1/auth/login"))
            .header("Origin","http://localhost:5173")
            .header("Access-Control-Request-Method","POST")
            .header("Access-Control-Request-Headers","Content-Type, X-CSRF-TOKEN")
            .method("OPTIONS",HttpRequest.BodyPublishers.noBody()).build(),HttpResponse.BodyHandlers.ofString());
        assertEquals(200,response.statusCode(),response.body());
        assertEquals("http://localhost:5173",response.headers().firstValue("Access-Control-Allow-Origin").orElse(""));
        assertEquals("true",response.headers().firstValue("Access-Control-Allow-Credentials").orElse(""));
    }
    @Test void loginJspAndAssetsCompileOnRealTomcat() throws Exception {
        var response=get("/login");
        assertEquals(200,response.statusCode(),response.body());
        assertTrue(response.body().contains("Bắt đầu một ca mới."));
        assertTrue(response.body().contains("/polycafe/assets/cafe.css"));
        assertEquals(200,get("/assets/cafe.js").statusCode());
        assertEquals(302,get("/app/overview").statusCode());
        assertFalse(get("/WEB-INF/views/login.jsp").statusCode()==200);
    }
    @Test void everyScreenCompilesAndEscapesUserContent() throws Exception {
        login();
        for(String page:List.of("overview","pos","orders","tables","kitchen","tickets","menu","inventory","purchasing","customers","shifts","reports","settings")) {
            var response=get("/app/"+page);
            assertEquals(200,response.statusCode(),page+": "+response.body());
            assertTrue(response.body().contains("&lt;script&gt;unsafe&lt;/script&gt;"),page);
            assertFalse(response.body().contains("<script>unsafe</script>"),page);
            assertTrue(response.body().contains("/polycafe/assets/cafe.js"),page);
        }
        assertTrue(get("/app/overview").body().contains("Sẵn sàng cho những ly cà phê tiếp theo?"));
        assertTrue(get("/app/overview").headers().firstValue("Content-Type").orElse("").toLowerCase().contains("charset=utf-8"));
        assertEquals(200,get("/app/data/ingredient").statusCode());
        assertEquals(404,get("/app/not-a-screen").statusCode());
    }
}

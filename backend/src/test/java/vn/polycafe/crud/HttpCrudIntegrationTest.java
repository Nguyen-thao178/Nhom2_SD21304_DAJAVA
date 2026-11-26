package vn.polycafe.crud;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import vn.polycafe.auth.StaffUser;
import java.util.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties={
    "spring.datasource.url=jdbc:h2:mem:crud;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
    "spring.datasource.username=sa","spring.datasource.password=",
    "spring.datasource.driver-class-name=org.h2.Driver","spring.datasource.hikari.connection-init-sql=",
    "app.bootstrap.username=","app.bootstrap.password="})
@AutoConfigureMockMvc
public class HttpCrudIntegrationTest {
    @Autowired protected MockMvc http;
    @Autowired protected JdbcTemplate db;
    @Autowired protected ObjectMapper json;
    protected MockHttpSession session;
    public static void lock(long value) { /* H2 substitute; PostgreSQL lock semantics require live DB tests. */ }
    @BeforeEach void setup() {
        db.execute("CREATE ALIAS IF NOT EXISTS pg_advisory_xact_lock FOR 'vn.polycafe.crud.HttpCrudIntegrationTest.lock'");
        db.execute("CREATE TABLE IF NOT EXISTS app_user(id uuid primary key,username varchar(60),full_name varchar(150),password_hash varchar(255),status varchar(20),account_kind varchar(20),failed_count int,refresh_version int,locked_until timestamp with time zone)");
        db.execute("CREATE TABLE IF NOT EXISTS user_access(id uuid primary key,user_id uuid,role varchar(20),branch_id uuid,status varchar(20))");
        db.execute("CREATE TABLE IF NOT EXISTS ingredient(id uuid primary key,name varchar(150) not null,base_unit varchar(20) not null,min_stock numeric(14,4),shelf_life_days int,status varchar(20) not null,created_at timestamp with time zone default now(),updated_at timestamp with time zone default now())");
        db.execute("CREATE TABLE IF NOT EXISTS audit_log(id uuid default random_uuid() primary key,actor_id uuid,action varchar(50),entity_type varchar(40),entity_id uuid,before_data jsonb,after_data jsonb,request_id varchar(64),created_at timestamp with time zone default now())");
        db.update("DELETE FROM audit_log");db.update("DELETE FROM ingredient");db.update("DELETE FROM user_access");db.update("DELETE FROM app_user");
        UUID id=UUID.randomUUID();
        db.update("INSERT INTO app_user VALUES(?,?,?,'unused','active','staff',0,0,NULL)",id,"test-owner","Owner");
        db.update("INSERT INTO user_access VALUES(?,?,'owner',NULL,'active')",UUID.randomUUID(),id);
        session=new MockHttpSession();session.setAttribute("staff",new StaffUser(id,"test-owner","Owner",0,Set.of("owner"),true));
    }
    @Test void realHttpCreateReadUpdateArchiveAndAudit() throws Exception {
        String created=http.perform(post("/api/v1/ingredient").session(session).with(csrf()).contentType("application/json")
            .content("{\"name\":\"Milk\",\"base_unit\":\"ml\",\"status\":\"active\"}"))
            .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        var record=json.readTree(created);String id=record.get("id").asText();
        http.perform(get("/api/v1/ingredient/"+id).session(session)).andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Milk"));
        String updated=http.perform(patch("/api/v1/ingredient/"+id).session(session).with(csrf()).contentType("application/json")
            .header("X-Expected-Updated-At",record.get("updated_at").asText()).content("{\"name\":\"Milk revised\"}"))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        http.perform(delete("/api/v1/ingredient/"+id).session(session).with(csrf())
            .header("X-Expected-Updated-At",json.readTree(updated).get("updated_at").asText())).andExpect(status().isNoContent());
        assertEquals("inactive",db.queryForObject("SELECT status FROM ingredient WHERE id=?",String.class,UUID.fromString(id)));
        assertEquals(3,db.queryForObject("SELECT count(*) FROM audit_log",Integer.class));
    }
    @Test void rejectsAnonymousCsrfXmlAndWrongTypes() throws Exception {
        http.perform(get("/api/v1/ingredient")).andExpect(status().isUnauthorized());
        http.perform(post("/api/v1/ingredient").session(session).contentType("application/json").content("{}"))
            .andExpect(status().isForbidden());
        http.perform(post("/api/v1/ingredient").session(session).with(csrf()).contentType("application/xml").content("<!DOCTYPE x [<!ENTITY x SYSTEM 'file:///etc/passwd'>]><x>&x;</x>"))
            .andExpect(status().isUnsupportedMediaType());
        http.perform(post("/api/v1/ingredient").session(session).with(csrf()).contentType("application/json").content("{\"name\":{},\"base_unit\":\"ml\",\"status\":\"active\"}"))
            .andExpect(status().isBadRequest());
    }
    @Test void sqlInjectionIsStoredAsText() throws Exception {
        String name="x'; DROP TABLE app_user; --";
        http.perform(post("/api/v1/ingredient").session(session).with(csrf()).contentType("application/json")
            .content(json.writeValueAsString(Map.of("name",name,"base_unit","ml","status","active"))))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.name").value(name));
        assertEquals(1,db.queryForObject("SELECT count(*) FROM app_user",Integer.class));
    }
}

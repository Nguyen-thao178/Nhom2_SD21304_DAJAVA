package vn.polycafe.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class AuditService {
    private final JdbcTemplate db;
    private final ObjectMapper json;
    public AuditService(JdbcTemplate db, ObjectMapper json) { this.db=db; this.json=json; }
    public void record(UUID actor, String action, String entity, UUID id, Object before, Object after) {
        try {
            db.update("INSERT INTO audit_log(actor_id,action,entity_type,entity_id,before_data,after_data,request_id) VALUES(?,?,?,?,CAST(? AS jsonb),CAST(? AS jsonb),?)",
                actor,action,entity,id,before==null?null:json.writeValueAsString(before),after==null?null:json.writeValueAsString(after),UUID.randomUUID().toString());
        } catch(JsonProcessingException e) { throw new IllegalStateException("Cannot serialize audit data",e); }
    }
}

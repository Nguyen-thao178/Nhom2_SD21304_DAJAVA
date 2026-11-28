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
    public void detail(UUID actor,UUID branch,String action,String entity,UUID id,Object before,Object after,String reason) {
        try {
            String device="web";String requestId=UUID.randomUUID().toString();
            var attributes=org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
            if(attributes instanceof org.springframework.web.context.request.ServletRequestAttributes a){
                var session=a.getRequest().getSession(false);
                if(session!=null)device="web-"+Integer.toUnsignedString(session.getId().hashCode(),16);
                Object trace=a.getRequest().getAttribute("requestId");if(trace!=null)requestId=trace.toString();
            }
            db.update("INSERT INTO audit_log(actor_id,branch_id,device_id,action,entity_type,entity_id,before_data,after_data,reason,request_id) VALUES(?,?,?,?,?,?,CAST(? AS jsonb),CAST(? AS jsonb),?,?)",
                actor,branch,device,action,entity,id,before==null?null:json.writeValueAsString(redact(before)),after==null?null:json.writeValueAsString(redact(after)),reason,requestId);
        }catch(JsonProcessingException e){throw new IllegalStateException(e);}
    }
    public Object redact(Object value) {
        com.fasterxml.jackson.databind.JsonNode node=json.valueToTree(value);
        redactNode(node);return json.convertValue(node,Object.class);
    }
    private void redactNode(com.fasterxml.jackson.databind.JsonNode node){
        if(node.isObject()){
            var object=(com.fasterxml.jackson.databind.node.ObjectNode)node;var fields=new java.util.ArrayList<String>();object.fieldNames().forEachRemaining(fields::add);
            for(String key:fields){String lower=key.toLowerCase(java.util.Locale.ROOT);
                if(lower.contains("password")||lower.contains("pin_hash")||lower.contains("secret")||lower.contains("token"))object.remove(key);else redactNode(object.get(key));}
        }else if(node.isArray())node.forEach(this::redactNode);
    }
}

package vn.polycafe.services;

import com.fasterxml.jackson.databind.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.function.Supplier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.polycafe.auth.StaffUser;
import vn.polycafe.common.ApiException;
import vn.polycafe.dao.OperationRepository;

/** The command, audit, journal changes and replay result commit together. */
@Service
public class CommandService {
    private final OperationRepository db;private final ObjectMapper json;
    public CommandService(OperationRepository db,ObjectMapper json){this.db=db;this.json=json;}
    private String hash(String input){try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(input.getBytes(StandardCharsets.UTF_8)));}catch(Exception e){throw new IllegalStateException(e);}}
    @SuppressWarnings("unchecked") @Transactional
    public Map<String,Object> execute(StaffUser actor,UUID branch,String action,UUID requestId,Object input,Supplier<Map<String,Object>> work){
        if(requestId==null)throw new ApiException(400,"Thiếu mã chống gửi trùng.");
        String key="command:"+hash(actor.id()+":"+action+":"+requestId),fingerprint=hash(db.json(input));db.advisory(key);
        var existing=db.rows("SELECT payload_hash,result,status FROM async_operation WHERE dedupe_key=?",key);
        if(!existing.isEmpty()){
            var row=existing.get(0);if(!fingerprint.equals(row.get("payload_hash")))throw new ApiException(409,"Mã yêu cầu đã dùng với nội dung khác.");
            if(!"succeeded".equals(row.get("status")))throw new ApiException(409,"Yêu cầu đang được xử lý.");
            return (Map<String,Object>)row.get("result");
        }
        UUID operation=UUID.randomUUID();db.write("INSERT INTO async_operation(id,kind,branch_id,actor_id,dedupe_key,payload_hash,payload,status) VALUES(?,'sync',?,?,?,?,CAST(? AS jsonb),'processing')",operation,branch,actor.id(),key,fingerprint,db.json(Map.of("schema_version",1,"action",action,"input",input)));
        Map<String,Object> result=work.get();db.write("UPDATE async_operation SET status='succeeded',result=CAST(? AS jsonb) WHERE id=?",db.json(result),operation);
        return result;
    }
}

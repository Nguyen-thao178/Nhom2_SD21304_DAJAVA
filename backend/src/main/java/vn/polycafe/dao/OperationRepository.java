package vn.polycafe.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.sql.*;
import java.time.OffsetDateTime;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import vn.polycafe.common.ApiException;

/** SQL is supplied by server services. HTTP values are always JDBC parameters. */
@Repository
public class OperationRepository {
    private final JdbcTemplate jdbc;
    private final ObjectMapper json;
    private static final Set<String> LOCKABLE=Set.of("invoice","invoice_line","ticket","pager_device","payment","approval_request","shift","customer","dining_table","stock_lot","app_user");
    public OperationRepository(JdbcTemplate jdbc,ObjectMapper json){this.jdbc=jdbc;this.json=json;}
    public List<Map<String,Object>> rows(String sql,Object... args){
        return jdbc.query(sql,(r,n)->{var m=new LinkedHashMap<String,Object>();var md=r.getMetaData();for(int i=1;i<=md.getColumnCount();i++){
            Object v=r.getObject(i);String type=md.getColumnTypeName(i);
            if(v!=null && type.equals("timestamptz"))v=r.getObject(i,OffsetDateTime.class);
            if(v!=null && type.equals("jsonb"))try{v=json.readValue(r.getString(i),Object.class);}catch(Exception e){throw new SQLException("Invalid JSON data",e);}
            m.put(md.getColumnLabel(i).toLowerCase(Locale.ROOT),v);
        }return m;},args);
    }
    public Map<String,Object> one(String sql,Object... args){var rows=rows(sql,args);if(rows.isEmpty())throw new ApiException(404,"Không tìm thấy dữ liệu trong phạm vi được cấp.");return rows.get(0);}
    public Map<String,Object> lock(String table,UUID id){if(!LOCKABLE.contains(table))throw new IllegalArgumentException("Invalid server table");return one("SELECT * FROM "+table+" WHERE id=? FOR UPDATE",id);}
    public int write(String sql,Object... args){return jdbc.update(sql,args);}
    public void advisory(String key){rows("SELECT pg_advisory_xact_lock(hashtextextended(?,0))",key);}
    public String json(Object value){try{return json.writeValueAsString(value);}catch(Exception e){throw new IllegalStateException(e);}}
    public static BigDecimal money(Object value){return value==null?BigDecimal.ZERO:new BigDecimal(value.toString());}
    public static UUID id(Map<String,Object> row,String key){Object v=row.get(key);return v==null?null:UUID.fromString(v.toString());}
}

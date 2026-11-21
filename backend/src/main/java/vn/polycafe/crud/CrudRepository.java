package vn.polycafe.crud;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import vn.polycafe.common.ApiException;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class CrudRepository {
    private final JdbcTemplate db;
    private final ObjectMapper json;
    public CrudRepository(JdbcTemplate db, ObjectMapper json) { this.db = db; this.json = json; }

    private RowMapper<Map<String,Object>> mapper(SchemaCatalog.Table table) {
        return (rs, row) -> {
            Map<String,Object> result = new LinkedHashMap<>();
            for (var c : table.columns()) {
                Object value = rs.getObject(c.name());
                if (value != null && c.type().equals("timestamptz")) value = rs.getObject(c.name(), java.time.OffsetDateTime.class);
                if (value != null && c.type().equals("jsonb")) {
                    try { value = json.readTree(rs.getString(c.name())); }
                    catch (Exception e) { throw new SQLException("Invalid stored JSON", e); }
                } else if (value instanceof java.sql.Array array) {
                    value = Arrays.asList((Object[]) array.getArray());
                    array.free();
                }
                result.put(c.name(), value);
            }
            return result;
        };
    }
    // Table and column names originate exclusively in SchemaCatalog. Values are always bound.
    public List<Map<String,Object>> list(SchemaCatalog.Table t, UUID after, int size) {
        return after == null
            ? db.query("SELECT * FROM public." + t.name() + " ORDER BY id LIMIT ?", mapper(t), size)
            : db.query("SELECT * FROM public." + t.name() + " WHERE id>? ORDER BY id LIMIT ?", mapper(t), after, size);
    }
    public Map<String,Object> get(SchemaCatalog.Table t, UUID id, boolean lock) {
        return db.query("SELECT * FROM public." + t.name() + " WHERE id=?" + (lock ? " FOR UPDATE" : ""), mapper(t), id)
            .stream().findFirst().orElseThrow(() -> new ApiException(404, "Không tìm thấy dữ liệu."));
    }
    private String placeholder(SchemaCatalog.Column c) {
        return switch (c.type()) {
            case "jsonb" -> "CAST(? AS jsonb)";
            case "_uuid" -> "CAST(? AS uuid[])";
            default -> "?";
        };
    }
    public UUID insert(SchemaCatalog.Table t, Map<String,Object> values) {
        UUID id = UUID.randomUUID();
        var fields = new LinkedHashMap<String,Object>();
        fields.put("id", id); fields.putAll(values);
        String names = String.join(",", fields.keySet());
        String parameters = fields.keySet().stream().map(k -> placeholder(t.column(k))).collect(Collectors.joining(","));
        db.update("INSERT INTO public." + t.name() + " (" + names + ") VALUES (" + parameters + ")", fields.values().toArray());
        return id;
    }
    public void update(SchemaCatalog.Table t, UUID id, Map<String,Object> values) {
        if (values.isEmpty()) throw new ApiException(400, "Không có thuộc tính để cập nhật.");
        String assignments = values.keySet().stream().map(k -> k + "=" + placeholder(t.column(k))).collect(Collectors.joining(","));
        if (t.has("updated_at")) assignments += ",updated_at=CURRENT_TIMESTAMP";
        var args = new ArrayList<>(values.values()); args.add(id);
        db.update("UPDATE public." + t.name() + " SET " + assignments + " WHERE id=?", args.toArray());
    }
    public void delete(SchemaCatalog.Table t, UUID id) { db.update("DELETE FROM public." + t.name() + " WHERE id=?", id); }
}

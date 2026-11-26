package vn.polycafe.crud;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.polycafe.auth.StaffUser;
import vn.polycafe.common.*;
import java.nio.charset.StandardCharsets;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;

@Service
public class CrudService {
    private static final Set<String> APPEND_ONLY = Set.of("audit_log", "stock_movement", "point_ledger", "fulfillment_event", "invoice_line_ingredient");
    private static final Set<String> SERVER_FIELDS = Set.of("id", "created_at", "updated_at", "password_hash", "pin_hash", "refresh_version", "failed_count", "locked_until");
    private static final Map<String,String> DRAFT_STATUS = Map.of("invoice","draft", "invoice_line","pending_accept", "stock_document","draft", "purchase_order","requested", "recipe","draft", "payment","pending", "approval_request","pending");
    private static final Map<String,String> ARCHIVE_STATUS = Map.ofEntries(
        Map.entry("menu_item","archived"), Map.entry("menu_variant","inactive"), Map.entry("branch","inactive"),
        Map.entry("station","inactive"), Map.entry("dining_table","inactive"), Map.entry("ingredient","inactive"),
        Map.entry("supplier","inactive"), Map.entry("app_user","locked"), Map.entry("user_access","inactive"),
        Map.entry("integration_config","inactive"), Map.entry("invoice","cancelled"), Map.entry("invoice_line","canceled"),
        Map.entry("purchase_order","cancelled"), Map.entry("stock_document","cancelled"), Map.entry("payment","failed"), Map.entry("recipe","retired"));
    private final SchemaCatalog catalog;
    private final CrudRepository repository;
    private final PayloadValidator validator;
    private final AuditService audit;
    private final PasswordEncoder passwords;
    private final JdbcTemplate db;
    public CrudService(SchemaCatalog catalog, CrudRepository repository, PayloadValidator validator,
                       AuditService audit, PasswordEncoder passwords, JdbcTemplate db) {
        this.catalog=catalog; this.repository=repository; this.validator=validator; this.audit=audit; this.passwords=passwords; this.db=db;
    }
    public void authorize(StaffUser user) {
        if (user == null || !user.canManageProducts()) throw new ApiException(403,"API quản trị dữ liệu yêu cầu owner/sysadmin toàn dự án.");
    }
    public Object metadata(String table, StaffUser user) { authorize(user); return catalog.table(table); }
    public Collection<SchemaCatalog.Table> metadata(StaffUser user) { authorize(user); return catalog.tables(); }
    public List<Map<String,Object>> list(String table, UUID after, int size, StaffUser user) {
        authorize(user);
        if (size<1 || size>100) throw new ApiException(400,"size từ 1 đến 100.");
        return repository.list(catalog.table(table),after,size).stream().map(this::safe).toList();
    }
    public Map<String,Object> get(String table, UUID id, StaffUser user) {
        authorize(user); return safe(repository.get(catalog.table(table),id,false));
    }
    private Map<String,Object> safe(Map<String,Object> row) {
        var result = new LinkedHashMap<>(row);
        for (String field : Set.of("password_hash","pin_hash","secret_ref","before_data","after_data","payload","result","last_error")) result.remove(field);
        return result;
    }
    private Map<String,Object> input(SchemaCatalog.Table table, Map<String,Object> body, boolean create, StaffUser user) {
        var raw = new LinkedHashMap<>(body);
        Object password=raw.remove("password");
        for (String field : raw.keySet()) {
            if (SERVER_FIELDS.contains(field)) throw new ApiException(400,"Thuộc tính do server quản lý: "+field);
        }
        var values=raw.isEmpty() && password!=null ? new LinkedHashMap<String,Object>() : validator.validate(table,raw);
        if (password!=null) {
            if (!table.name().equals("app_user") || !(password instanceof String str)) throw new ApiException(400,"Sai trường password.");
            int length=str.getBytes(StandardCharsets.UTF_8).length;
            if(length<12 || length>72) throw new ApiException(400,"Mật khẩu phải có 12–72 byte UTF-8.");
            values.put("password_hash",passwords.encode(str));
        }
        if (create && table.name().equals("app_user") && password==null) throw new ApiException(400,"Thiếu password.");
        if (create) {
            Set<String> managed = Set.of("paid_total","points_balance","qty_remaining","reserved_qty","handed_qty","used_count","qty_received","qty_rejected","paid_amount","approved_by","approver_id","balance_after","paid_at","decided_at","returned_at","closed_at","accepted_by","prepared_by","ready_by","started_at","ready_at","confirmed_at","confirmed_by","einvoice_data","delivery_data","is_compensation","merged_into_invoice_id","compensates_invoice_id");
            for (String key : managed) if (values.containsKey(key)) throw new ApiException(400,"Thuộc tính nghiệp vụ do server quản lý: "+key);
            if(table.name().equals("point_ledger")) values.put("balance_after",0);
            if(table.name().equals("stock_lot")) values.put("qty_remaining",BigDecimal.ZERO);
            if(table.name().equals("invoice")) {
                values.put("paid_total",BigDecimal.ZERO);
                values.putIfAbsent("discount_snapshot","{\"schema_version\":1,\"promotions\":[]}");
            }
            for (String field : List.of("actor_id","created_by","requested_by","issued_by","opened_by","staff_id")) {
                if (table.has(field)) values.put(field,user.id());
            }
            if (table.name().equals("audit_log")) values.put("request_id",UUID.randomUUID().toString());
            if (DRAFT_STATUS.containsKey(table.name())) {
                String draft=DRAFT_STATUS.get(table.name());
                if(values.containsKey("status") && !draft.equals(values.get("status"))) throw new ApiException(409,"Chứng từ mới phải ở trạng thái "+draft);
                values.put("status",draft);
            }
        }
        if (create) for (var c : table.columns()) {
            if (!SERVER_FIELDS.contains(c.name()) && !c.nullable() && !c.hasDefault() && !values.containsKey(c.name()))
                throw new ApiException(400,"Thiếu thuộc tính "+c.name());
        }
        return values;
    }
    @Transactional
    public Map<String,Object> create(String name, Map<String,Object> body, StaffUser user) {
        authorize(user);
        var table=catalog.table(name);
        var values=input(table,body,true,user);
        // A shared transaction lock serializes related administrative edits, including parent checks.
        lockAdministration();
        requireDraft(name,values);
        if (name.equals("invoice_line_ingredient")) throw new ApiException(409,"Snapshot nguyên liệu chỉ được sinh từ nghiệp vụ xác nhận đơn.");
        if (name.equals("point_ledger")) applyPoints(values);
        if (name.equals("stock_movement")) applyStock(values);
        if (name.equals("fulfillment_event")) applyHandover(values);
        UUID id=repository.insert(table,values);
        var result=repository.get(table,id,false);
        if (!name.equals("audit_log")) audit.record(user.id(),"crud.create",name,id,null,safe(result));
        return safe(result);
    }
    @Transactional
    public Map<String,Object> update(String name, UUID id, Map<String,Object> body, OffsetDateTime expected, StaffUser user) {
        authorize(user); var table=catalog.table(name); requireMutable(name); lockAdministration();
        var before=repository.get(table,id,true); matchVersion(before,expected); requireDraft(name,before);
        protectOwnAccount(name,before,user);
        var values=input(table,body,false,user);
        protectLifecycle(name,before,values);
        var merged = new LinkedHashMap<>(before); merged.putAll(values); requireDraft(name,merged);
        if (name.equals("app_user") && values.containsKey("password_hash")) {
            values.put("refresh_version",((Number)before.get("refresh_version")).intValue()+1);
        }
        repository.update(table,id,values);
        var result=repository.get(table,id,false);
        audit.record(user.id(),"crud.update",name,id,safe(before),safe(result));return safe(result);
    }
    @Transactional
    public void delete(String name, UUID id, OffsetDateTime expected, StaffUser user) {
        authorize(user); var table=catalog.table(name); requireMutable(name); lockAdministration();
        var before=repository.get(table,id,true); matchVersion(before,expected); requireDraft(name,before);
        protectOwnAccount(name,before,user);
        if (Set.of("shift","ticket","dining_session","customer","stock_lot","cash_entry","async_operation").contains(name))
            throw new ApiException(409,"Dữ liệu cần đóng, đối soát hoặc điều chỉnh bằng nghiệp vụ, không xóa trực tiếp.");
        if (ARCHIVE_STATUS.containsKey(name)) repository.update(table,id,Map.of("status",ARCHIVE_STATUS.get(name)));
        else repository.delete(table,id);
        audit.record(user.id(),"crud.delete",name,id,safe(before),null);
    }
    private void lockAdministration() { db.execute("SELECT pg_advisory_xact_lock(726519301)"); }
    private void protectOwnAccount(String name,Map<String,Object> row,StaffUser user) {
        if(name.equals("app_user") && user.id().equals(row.get("id")) || name.equals("user_access") && user.id().equals(row.get("user_id")))
            throw new ApiException(409,"Dùng tài khoản quản trị khác để thay đổi tài khoản hoặc quyền của chính mình.");
    }
    private void requireMutable(String name) {
        if(APPEND_ONLY.contains(name)) throw new ApiException(405,"Bảng chỉ thêm. Sửa sai bằng bút toán hoặc sự kiện bổ sung.");
    }
    private void matchVersion(Map<String,Object> row, OffsetDateTime expected) {
        Object actual=row.get("updated_at");
        if(actual==null) actual=row.get("created_at");
        if(expected==null || !OffsetDateTime.parse(actual.toString().replace(' ','T')).toInstant().equals(expected.toInstant()))
            throw new ApiException(409,"Thiếu hoặc sai X-Expected-Updated-At. Tải bản ghi mới nhất.");
    }
    private void requireDraft(String table, Map<String,Object> row) {
        if(DRAFT_STATUS.containsKey(table) && !DRAFT_STATUS.get(table).equals(row.get("status")))
            throw new ApiException(409,"Chứng từ đã chốt, cần thao tác nghiệp vụ chuyên biệt.");
        Map<String,String> parents=Map.of("invoice_line","invoice", "recipe_line","recipe", "purchase_order_line","purchase_order", "stock_document_line","stock_document");
        if(parents.containsKey(table)) {
            String parent=parents.get(table);
            String fk=table.equals("stock_document_line")?"document_id":parent+"_id";
            var master=repository.get(catalog.table(parent),(UUID)row.get(fk),true);
            requireDraft(parent,master);
        }
    }
    private void protectLifecycle(String name, Map<String,Object> before, Map<String,Object> values) {
        Set<String> protectedFields = Set.of("status","paid_total","points_balance","qty_remaining","reserved_qty","handed_qty","used_count","approved_by","approver_id","balance_after","approved_at","paid_at","decided_at","returned_at","closed_at","accepted_by","prepared_by","ready_by","started_at","ready_at","confirmed_at","confirmed_by","einvoice_data","delivery_data","actor_id","created_by","requested_by","issued_by","opened_by","staff_id","qty_received","qty_rejected","paid_amount");
        for(String key:values.keySet()) if(protectedFields.contains(key) && !Objects.equals(values.get(key),before.get(key)))
            throw new ApiException(409,"Thuộc tính cần nghiệp vụ chuyên biệt: "+key);
        if(name.equals("stock_lot") || name.equals("cash_entry")) throw new ApiException(409,"Lô kho và thu chi đã ghi nhận phải điều chỉnh bằng chứng từ.");
    }
    private BigDecimal decimal(Object value) { return new BigDecimal(value.toString()); }
    private void applyPoints(Map<String,Object> v) {
        if(!"adjust".equals(v.get("kind"))) throw new ApiException(409,"CRUD sổ điểm chỉ nhận bút toán adjust; tích, đổi và hết hạn cần nghiệp vụ tương ứng.");
        UUID id=(UUID)v.get("customer_id");
        var customer=repository.get(catalog.table("customer"),id,true);
        if(!"member".equals(customer.get("kind"))) throw new ApiException(409,"Khách vãng lai không có điểm.");
        int next=Math.addExact(((Number)customer.get("points_balance")).intValue(),((Number)v.get("points")).intValue());
        if(next<0) throw new ApiException(409,"Không đủ điểm.");
        v.put("balance_after",next); db.update("UPDATE customer SET points_balance=? WHERE id=?",next,id);
    }
    private void applyStock(Map<String,Object> v) {
        UUID lotId=(UUID)v.get("lot_id");
        if(lotId==null) throw new ApiException(400,"Bút toán kho qua API yêu cầu lot_id.");
        var lot=repository.get(catalog.table("stock_lot"),lotId,true);
        if(!Objects.equals(lot.get("branch_id"),v.get("branch_id")) || !Objects.equals(lot.get("ingredient_id"),v.get("ingredient_id"))) throw new ApiException(409,"Lô không thuộc nguyên liệu/chi nhánh.");
        BigDecimal qty=decimal(lot.get("qty_remaining")), reserved=decimal(lot.get("reserved_qty")), delta=decimal(v.get("qty_base"));
        String kind=(String)v.get("kind");
        if(kind.equals("reserve") || kind.equals("release")) {
            if(kind.equals("reserve") && delta.signum()<=0 || kind.equals("release") && delta.signum()>=0) throw new ApiException(400,"Sai dấu giữ chỗ/giải phóng.");
            reserved=reserved.add(delta);
        } else {
            if(Set.of("receipt","transfer_in").contains(kind) && delta.signum()<=0 || Set.of("issue","consume","waste","transfer_out").contains(kind) && delta.signum()>=0) throw new ApiException(400,"Sai dấu bút toán.");
            qty=qty.add(delta);
        }
        if(qty.signum()<0 || reserved.signum()<0 || reserved.compareTo(qty)>0) throw new ApiException(409,"Tồn kho hoặc giữ chỗ không hợp lệ.");
        db.update("UPDATE stock_lot SET qty_remaining=?,reserved_qty=? WHERE id=?",qty,reserved,lotId);
    }
    private void applyHandover(Map<String,Object> v) {
        if(!"succeeded".equals(v.get("result"))) return;
        if(!Set.of("customer_handover","driver_handover").contains(v.get("kind"))) return;
        UUID lineId=(UUID)v.get("line_id");
        if(lineId==null) throw new ApiException(400,"Thiếu line_id khi giao món.");
        var line=repository.get(catalog.table("invoice_line"),lineId,true);
        if(!"ready".equals(line.get("status"))) throw new ApiException(409,"Món chưa sẵn sàng hoặc đã giao đủ.");
        int count=((Number)v.get("quantity")).intValue();
        int next=((Number)line.get("handed_qty")).intValue()+count;
        if(count<1 || next>((Number)line.get("quantity")).intValue()) throw new ApiException(409,"Số lượng giao vượt số món.");
        db.update("UPDATE invoice_line SET handed_qty=?,status=? WHERE id=?",next,next==((Number)line.get("quantity")).intValue()?"handed_over":"ready",lineId);
    }
}

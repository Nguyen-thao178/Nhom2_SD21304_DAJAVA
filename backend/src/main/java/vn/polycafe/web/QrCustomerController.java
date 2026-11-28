package vn.polycafe.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.qrcode.QRCodeWriter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.*;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import vn.polycafe.auth.StaffUser;
import vn.polycafe.common.*;

/** Public table menu, member-owned orders, and staff-scoped printable QR codes. */
@RestController
public class QrCustomerController {
    public record Line(@NotNull UUID itemId,@Min(1) @Max(20) int quantity) {}
    public record Order(@NotNull UUID requestId,@NotEmpty @Size(max=30) List<@Valid Line> lines,@Size(max=300) String note) {}
    static String requestKey(UUID user,UUID request) {
        try {return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(("qr-member-"+user+"-"+request).getBytes(StandardCharsets.UTF_8)));}
        catch(java.security.NoSuchAlgorithmException impossible){throw new IllegalStateException(impossible);}
    }
    private final JdbcTemplate db;
    private final AuditService audit;
    private final ObjectMapper json;
    private final String publicBase;
    public QrCustomerController(JdbcTemplate db,AuditService audit,ObjectMapper json,
            @Value("${app.public-base-url:http://localhost:8080}") String publicBase) {
        this.db=db;this.audit=audit;this.json=json;
        URI uri=URI.create(publicBase);
        if(!Set.of("http","https").contains(uri.getScheme()) || uri.getHost()==null || uri.getUserInfo()!=null || uri.getQuery()!=null || uri.getFragment()!=null)
            throw new IllegalArgumentException("app.public-base-url must be an absolute HTTP(S) base URL");
        this.publicBase=publicBase.replaceAll("/+$","");
    }
    private Map<String,Object> table(String token,boolean lock) {
        if(token.length()>200) throw new ApiException(404,"Mã QR không hợp lệ.");
        var rows=db.queryForList("SELECT t.id,t.branch_id,t.code,t.status,b.name branch_name,b.timezone,b.business_day_cutoff FROM dining_table t JOIN branch b ON b.id=t.branch_id WHERE t.qr_token=? AND t.status<>'inactive' AND b.status='active'"+(lock?" FOR UPDATE OF t":""),token);
        if(rows.isEmpty()) throw new ApiException(404,"Mã QR không tồn tại hoặc bàn đã ngừng phục vụ.");
        return rows.get(0);
    }
    private Map<String,Object> member(StaffUser user) {
        if(user==null || !user.roles().contains("customer")) throw new ApiException(403,"Vui lòng đăng nhập tài khoản thành viên.");
        var rows=db.queryForList("SELECT id,member_code,full_name,tier,points_balance FROM customer WHERE user_id=? AND kind='member' AND status='active'",user.id());
        if(rows.isEmpty()) throw new ApiException(403,"Hồ sơ thành viên không hoạt động.");
        return rows.get(0);
    }
    private List<Map<String,Object>> menu(UUID branch) {
        return db.queryForList("""
            SELECT m.id,m.name,m.description,m.category_name,m.allergen_note,p.price
            FROM menu_item m JOIN LATERAL (
              SELECT price FROM price_rule p WHERE p.item_id=m.id AND (p.branch_id=? OR p.branch_id IS NULL)
              AND (p.channel='qr' OR p.channel IS NULL) AND p.valid_from<=CURRENT_TIMESTAMP
              AND (p.valid_to IS NULL OR p.valid_to>CURRENT_TIMESTAMP)
              ORDER BY p.branch_id NULLS LAST,p.channel NULLS LAST,p.valid_from DESC,p.id LIMIT 1
            ) p ON true WHERE m.status='active' AND m.kind IN ('drink','food')
            AND (m.valid_from IS NULL OR m.valid_from<=CURRENT_TIMESTAMP)
            AND (m.valid_to IS NULL OR m.valid_to>CURRENT_TIMESTAMP)
            ORDER BY m.category_name,m.name,m.id LIMIT 200
            """,branch);
    }
    @GetMapping("/api/qr/{token}/menu") @Transactional(readOnly=true)
    public Map<String,Object> menu(@PathVariable String token) {
        var t=table(token,false);
        return Map.of("table",t,"items",menu((UUID)t.get("branch_id")));
    }
    @GetMapping("/api/customer/profile")
    public Map<String,Object> profile(@AuthenticationPrincipal StaffUser user) {return member(user);}
    @GetMapping("/api/customer/orders")
    public Object orders(@AuthenticationPrincipal StaffUser user) {
        var c=member(user);
        return db.queryForList("SELECT i.id,i.invoice_no,i.status,i.total,i.paid_total,i.created_at,t.code table_code FROM invoice i LEFT JOIN dining_table t ON t.id=i.table_id WHERE i.customer_id=? ORDER BY i.created_at DESC,i.id LIMIT 50",c.get("id"));
    }
    @PostMapping(value="/api/qr/{token}/orders",consumes="application/json")
    @ResponseStatus(HttpStatus.CREATED) @Transactional
    public Map<String,Object> order(@PathVariable String token,@Valid @RequestBody Order request,@AuthenticationPrincipal StaffUser user) throws Exception {
        var t=table(token,true);var c=member(user);
        String fingerprint=HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(json.writeValueAsBytes(request)));
        String key=requestKey(user.id(),request.requestId());
        db.queryForList("SELECT pg_advisory_xact_lock(hashtextextended(?,0))",key);
        var old=db.queryForList("SELECT id,invoice_no,status,total,customer_id,table_id,discount_snapshot->>'fingerprint' fingerprint FROM invoice WHERE idempotency_key=?",key);
        if(!old.isEmpty()) {
            var row=old.get(0);
            if(!c.get("id").equals(row.get("customer_id")) || !t.get("id").equals(row.get("table_id")) || !fingerprint.equals(row.get("fingerprint"))) throw new ApiException(409,"Mã yêu cầu đã được dùng cho đơn khác.");
            return Map.of("id",row.get("id"),"invoiceNo",row.get("invoice_no"),"status",row.get("status"),"total",row.get("total"));
        }
        if(!Set.of("free","occupied").contains(t.get("status"))) throw new ApiException(409,"Bàn chưa sẵn sàng nhận đặt món.");
        UUID branch=(UUID)t.get("branch_id"),table=(UUID)t.get("id");
        var available=new HashMap<UUID,Map<String,Object>>();
        for(var item:menu(branch)) available.put((UUID)item.get("id"),item);
        var quantities=new LinkedHashMap<UUID,Integer>();
        for(var line:request.lines()) quantities.merge(line.itemId(),line.quantity(),Integer::sum);
        BigDecimal total=BigDecimal.ZERO;
        for(var line:quantities.entrySet()) {
            if(line.getValue()>20 || !available.containsKey(line.getKey())) throw new ApiException(409,"Món đã ngừng bán hoặc số lượng vượt giới hạn.");
            total=total.add(((BigDecimal)available.get(line.getKey()).get("price")).multiply(BigDecimal.valueOf(line.getValue())));
        }
        var sessions=db.queryForList("SELECT id FROM dining_session WHERE table_id=? AND status='open' ORDER BY opened_at DESC LIMIT 1",table);
        UUID session=sessions.isEmpty()?UUID.randomUUID():(UUID)sessions.get(0).get("id");
        if(sessions.isEmpty()) db.update("INSERT INTO dining_session(id,branch_id,table_id,status,opened_at) VALUES(?,?,?,'open',CURRENT_TIMESTAMP)",session,branch,table);
        db.update("UPDATE dining_table SET status='occupied' WHERE id=?",table);
        var now=ZonedDateTime.now(ZoneId.of(t.get("timezone").toString()));
        LocalDate day=now.toLocalTime().isBefore(LocalTime.parse(t.get("business_day_cutoff").toString()))?now.toLocalDate().minusDays(1):now.toLocalDate();
        UUID id=UUID.randomUUID();String number="QR"+id.toString().replace("-","").substring(0,18);
        String snapshot=json.writeValueAsString(Map.of("schema_version",1,"fingerprint",fingerprint));
        db.update("INSERT INTO invoice(id,branch_id,invoice_no,table_id,session_id,source,customer_id,channel,status,subtotal,discount_total,total,paid_total,idempotency_key,business_date,discount_snapshot) VALUES(?,?,?,?,?,'qr',?,'qr','draft',?,0,?,0,?,?,CAST(? AS jsonb))",id,branch,number,table,session,c.get("id"),total,total,key,day,snapshot);
        for(var line:quantities.entrySet()) db.update("INSERT INTO invoice_line(id,invoice_id,item_id,quantity,unit_price,options_snapshot,note,status,line_kind) VALUES(?,?,?,?,?,CAST(? AS jsonb),?,'pending_accept','sale')",UUID.randomUUID(),id,line.getKey(),line.getValue(),available.get(line.getKey()).get("price"),"{\"schema_version\":1}",request.note());
        audit.record(user.id(),"qr_order_request","invoice",id,null,Map.of("table",table,"total",total));
        return Map.of("id",id,"invoiceNo",number,"status","draft","total",total);
    }
    private void staff(StaffUser user) {
        if(user==null || user.roles().contains("customer")) throw new ApiException(403,"Chỉ nhân viên được truy cập.");
    }
    @GetMapping("/api/staff/qr-tables")
    public Object tables(@AuthenticationPrincipal StaffUser user) {
        staff(user);
        return db.queryForList("""
            SELECT t.id,t.code,t.qr_token,b.name branch_name FROM dining_table t JOIN branch b ON b.id=t.branch_id
            WHERE t.status<>'inactive' AND b.status='active' AND (? OR EXISTS(
              SELECT 1 FROM user_access a WHERE a.user_id=? AND a.branch_id=t.branch_id AND a.status='active'
              AND a.role IN ('staff','shift_lead','owner','sysadmin'))) ORDER BY b.name,t.code LIMIT 200
            """,user.canManageProducts(),user.id());
    }
    @GetMapping(value="/api/staff/qr-tables/{id}/image",produces="image/svg+xml")
    public String image(@PathVariable UUID id,@AuthenticationPrincipal StaffUser user) throws Exception {
        staff(user);
        @SuppressWarnings("unchecked") var rows=(List<Map<String,Object>>)tables(user);
        var row=rows.stream().filter(r->id.equals(r.get("id"))).findFirst().orElseThrow(()->new ApiException(404,"Không tìm thấy bàn trong phạm vi được cấp."));
        String url=publicBase+"/qr/"+java.net.URLEncoder.encode(row.get("qr_token").toString(),StandardCharsets.UTF_8);
        var matrix=new QRCodeWriter().encode(url,BarcodeFormat.QR_CODE,256,256);
        var svg=new StringBuilder("<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 256 256\"><rect width=\"256\" height=\"256\" fill=\"white\"/><path fill=\"black\" d=\"");
        for(int y=0;y<256;y++) for(int x=0;x<256;x++) if(matrix.get(x,y)) svg.append('M').append(x).append(' ').append(y).append("h1v1h-1z");
        return svg.append("\"/></svg>").toString();
    }
    @GetMapping("/api/staff/qr-orders")
    public Object requests(@AuthenticationPrincipal StaffUser user) {
        staff(user);
        return db.queryForList("""
            SELECT i.id,i.invoice_no,i.status,i.total,i.created_at,t.code table_code,c.full_name
            FROM invoice i JOIN dining_table t ON t.id=i.table_id JOIN customer c ON c.id=i.customer_id
            WHERE i.source='qr' AND (? OR EXISTS(SELECT 1 FROM user_access a
              WHERE a.user_id=? AND a.branch_id=i.branch_id AND a.status='active'
              AND a.role IN ('staff','shift_lead','owner','sysadmin')))
            ORDER BY i.created_at DESC,i.id LIMIT 100
            """,user.canManageProducts(),user.id());
    }
}

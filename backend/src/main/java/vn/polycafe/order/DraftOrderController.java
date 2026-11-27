package vn.polycafe.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import vn.polycafe.auth.StaffUser;
import vn.polycafe.crud.CrudService;
import vn.polycafe.common.ApiException;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@RestController
@RequestMapping("/api/v1/orders/drafts")
public class DraftOrderController {
    public record Line(@NotNull UUID itemId,@Min(1) @Max(99) int quantity) {}
    public record Draft(@NotNull UUID branchId,@NotNull UUID requestId,@NotEmpty @Size(max=50) List<@Valid Line> lines,@Size(max=300) String note) {}
    private final JdbcTemplate db;
    private final CrudService crud;
    private final com.fasterxml.jackson.databind.ObjectMapper json;
    public DraftOrderController(JdbcTemplate db,CrudService crud,com.fasterxml.jackson.databind.ObjectMapper json){this.db=db;this.crud=crud;this.json=json;}
    @PostMapping(consumes="application/json") @ResponseStatus(HttpStatus.CREATED) @Transactional
    public Map<String,Object> create(@Valid @RequestBody Draft request,@AuthenticationPrincipal StaffUser user) {
        crud.authorize(user);
        db.execute("SELECT pg_advisory_xact_lock(726519301)");
        String fingerprint;
        try {
            byte[] bytes=json.writeValueAsBytes(request);
            fingerprint=java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch(Exception error) {throw new IllegalStateException(error);}
        String key="pos-draft-"+request.requestId();
        var previous=db.queryForList("SELECT id,staff_id,discount_snapshot FROM invoice WHERE idempotency_key=?",key);
        if(!previous.isEmpty()) {
            var existing=previous.get(0);
            String storedFingerprint;
            try {
                Object raw=existing.get("discount_snapshot");
                var snapshot=raw instanceof byte[] bytes ? json.readTree(bytes) : json.readTree(raw.toString());
                if(snapshot.isTextual()) snapshot=json.readTree(snapshot.asText());
                storedFingerprint=snapshot.path("draft_fingerprint").asText();
            } catch(Exception error) {throw new ApiException(409,"Không đọc được dấu vết đơn cũ. Hãy tra cứu đơn trước khi thử lại.");}
            if(!user.id().equals(existing.get("staff_id")) || !fingerprint.equals(storedFingerprint)) throw new ApiException(409,"Mã yêu cầu đã được dùng cho một đơn khác.");
            return crud.get("invoice",(UUID)existing.get("id"),user);
        }
        var branches=db.queryForList("SELECT timezone,business_day_cutoff FROM branch WHERE id=? AND status='active' FOR UPDATE",request.branchId());
        if(branches.isEmpty()) throw new ApiException(409,"Chi nhánh đã ngừng hoạt động hoặc không tồn tại.");
        var branch=branches.get(0);
        var now=ZonedDateTime.now(ZoneId.of((String)branch.get("timezone")));
        LocalTime cutoff=LocalTime.parse(branch.get("business_day_cutoff").toString());
        LocalDate date=now.toLocalTime().isBefore(cutoff)?now.toLocalDate().minusDays(1):now.toLocalDate();
        BigDecimal total=BigDecimal.ZERO;
        var items=new ArrayList<Map<String,Object>>();
        for(Line line:request.lines()) {
            var rows=db.queryForList("""
                SELECT m.id,m.name,m.station_kind,(SELECT p.price FROM price_rule p
                  WHERE p.item_id=m.id AND (p.branch_id=? OR p.branch_id IS NULL)
                    AND (p.channel='counter' OR p.channel IS NULL)
                    AND p.valid_from<=CURRENT_TIMESTAMP AND (p.valid_to IS NULL OR p.valid_to>CURRENT_TIMESTAMP)
                  ORDER BY p.branch_id NULLS LAST,p.channel NULLS LAST,p.valid_from DESC LIMIT 1) price
                FROM menu_item m WHERE m.id=? AND m.status='active' AND m.kind IN ('drink','food') FOR UPDATE
                """,request.branchId(),line.itemId());
            if(rows.isEmpty() || rows.get(0).get("price")==null) throw new ApiException(409,"Món đã ngừng bán hoặc chưa có giá tại chi nhánh.");
            var source=rows.get(0);
            var item=new LinkedHashMap<String,Object>();
            item.put("id",source.get("id"));item.put("price",source.get("price"));
            item.put("quantity",line.quantity());items.add(item);
            total=total.add(((BigDecimal)item.get("price")).multiply(BigDecimal.valueOf(line.quantity())));
        }
        var guest=crud.create("customer",Map.of("member_code","G"+UUID.randomUUID().toString().replace("-","").substring(0,18),"kind","guest","status","active"),user);
        var body=new LinkedHashMap<String,Object>();
        body.put("branch_id",request.branchId().toString());body.put("invoice_no","N"+UUID.randomUUID().toString().replace("-","").substring(0,18));
        body.put("customer_id",guest.get("id").toString());body.put("source","staff");body.put("channel","counter");
        body.put("subtotal",total.toPlainString());body.put("discount_total","0");body.put("total",total.toPlainString());
        body.put("business_date",date.toString());body.put("idempotency_key",key);
        body.put("discount_snapshot",Map.of("schema_version",1,"promotions",List.of(),"draft_fingerprint",fingerprint));
        var invoice=crud.create("invoice",body,user);
        for(var item:items) {
            var line=new LinkedHashMap<String,Object>();
            line.put("invoice_id",invoice.get("id").toString());line.put("item_id",item.get("id").toString());
            line.put("quantity",item.get("quantity"));line.put("unit_price",item.get("price").toString());
            line.put("options_snapshot",Map.of("schema_version",1));line.put("line_kind","sale");
            if(request.note()!=null && !request.note().isBlank()) line.put("note",request.note());
            crud.create("invoice_line",line,user);
        }
        return invoice;
    }
}

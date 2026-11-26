package vn.polycafe.web;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.*;
import vn.polycafe.auth.StaffUser;
import vn.polycafe.crud.CrudService;
import vn.polycafe.common.ApiException;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/ui/reports")
public class ReportController {
    private final JdbcTemplate db;
    private final CrudService permissions;
    public ReportController(JdbcTemplate db,CrudService permissions){this.db=db;this.permissions=permissions;}
    @GetMapping @Transactional(readOnly=true,isolation=Isolation.REPEATABLE_READ)
    public Map<String,Object> report(@RequestParam LocalDate from,@RequestParam LocalDate to,
            @RequestParam(required=false) UUID branch,@AuthenticationPrincipal StaffUser user) {
        permissions.authorize(user);
        if(to.isBefore(from) || java.time.temporal.ChronoUnit.DAYS.between(from,to)>366) throw new ApiException(400,"Khoảng báo cáo tối đa 366 ngày.");
        var daily=db.queryForList("""
            WITH refunds AS (
              SELECT invoice_id,sum(amount) amount FROM payment WHERE kind='refund' AND status='succeeded' GROUP BY invoice_id
            ) SELECT i.business_date AS day, count(*) AS orders, sum(i.total) AS gross,
              sum(coalesce(r.amount,0)) AS refunds, sum(i.total-coalesce(r.amount,0)) AS net
            FROM invoice i LEFT JOIN refunds r ON r.invoice_id=i.id
            WHERE i.business_date>=? AND i.business_date<=? AND i.status IN ('paid','refunded') AND NOT i.is_compensation
              AND (CAST(? AS uuid) IS NULL OR i.branch_id=?)
            GROUP BY i.business_date ORDER BY i.business_date
            """,from,to,branch,branch);
        var collection=db.queryForList("""
            SELECT p.method,p.kind,sum(p.amount) AS amount FROM payment p JOIN invoice i ON i.id=p.invoice_id
            WHERE i.business_date>=? AND i.business_date<=? AND p.status='succeeded' AND p.kind IN ('collect','refund')
              AND (CAST(? AS uuid) IS NULL OR i.branch_id=?) GROUP BY p.method,p.kind ORDER BY p.method,p.kind
            """,from,to,branch,branch);
        return Map.of("daily",serializable(daily),"collections",serializable(collection),"from",from,"to",to,
            "basis","Doanh thu theo ngày kinh doanh của hóa đơn đã thu đủ; hoàn tiền gắn về hóa đơn gốc.");
    }
    private List<Map<String,Object>> serializable(List<Map<String,Object>> rows) {
        return rows.stream().map(row -> {
            Map<String,Object> values=new LinkedHashMap<>();
            row.forEach((key,value) -> values.put(key.toLowerCase(Locale.ROOT),value instanceof java.math.BigDecimal amount ? amount.toPlainString() : value));
            return values;
        }).toList();
    }
}

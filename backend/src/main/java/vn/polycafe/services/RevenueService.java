package vn.polycafe.services;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import vn.polycafe.auth.StaffUser;
import vn.polycafe.common.ApiException;
import vn.polycafe.dao.OperationRepository;

@Service
public class RevenueService {
    private final OperationRepository db;private final AccessService access;
    public RevenueService(OperationRepository db,AccessService access){this.db=db;this.access=access;}
    @Transactional(readOnly=true,isolation=Isolation.REPEATABLE_READ)
    public Map<String,Object> report(LocalDate from,LocalDate to,UUID branch,StaffUser actor){
        if(!to.isAfter(from)||java.time.temporal.ChronoUnit.DAYS.between(from,to)>366)throw new ApiException(400,"Chọn khoảng ngày [từ, đến), tối đa 366 ngày.");
        List<UUID> allowed=new ArrayList<>();
        if(branch!=null){access.require(actor,branch,"report.read");allowed.add(branch);}
        else for(var b:access.branches(actor)){UUID id=OperationRepository.id(b,"id");try{access.require(actor,id,"report.read");allowed.add(id);}catch(ApiException denied){if(denied.status!=403)throw denied;}}
        if(allowed.isEmpty())throw new ApiException(403,"Bạn chưa được cấp quyền báo cáo.");
        String ids="{"+String.join(",",allowed.stream().map(UUID::toString).toList())+"}";
        var daily=db.rows("""
            WITH refunds AS(SELECT invoice_id,sum(amount) amount FROM payment WHERE kind='refund' AND status='succeeded' GROUP BY invoice_id),
            costs AS(SELECT l.invoice_id,sum(-m.qty_base*x.unit_cost) cost,bool_or(m.id IS NOT NULL AND x.unit_cost IS NULL) missing_cost
              FROM invoice_line l LEFT JOIN stock_movement m ON m.ref_type='invoice_line' AND m.ref_id=l.id AND m.kind='consume'
              LEFT JOIN stock_lot x ON x.id=m.lot_id WHERE l.status<>'canceled' GROUP BY l.invoice_id),
            source AS(SELECT i.*,coalesce(r.amount,0) refund,c.cost,
              (coalesce(c.missing_cost,false) OR c.cost IS NULL OR EXISTS(SELECT 1 FROM invoice_line l WHERE l.invoice_id=i.id AND l.status<>'canceled'
                AND (l.recipe_id IS NULL OR l.started_at IS NULL OR NOT EXISTS(SELECT 1 FROM stock_movement m WHERE m.ref_type='invoice_line' AND m.ref_id=l.id AND m.kind='consume')))) missing
              FROM invoice i LEFT JOIN refunds r ON r.invoice_id=i.id LEFT JOIN costs c ON c.invoice_id=i.id
              WHERE i.branch_id=ANY(CAST(? AS uuid[])) AND i.business_date>=? AND i.business_date<?
                AND i.status IN ('confirmed','partially_paid','paid','refunded') AND NOT i.is_compensation)
            SELECT business_date day,count(*) FILTER(WHERE status IN ('paid','refunded')) orders,
              coalesce(sum(subtotal) FILTER(WHERE status IN ('paid','refunded')),0) gross,
              coalesce(sum(discount_total) FILTER(WHERE status IN ('paid','refunded')),0) discounts,
              coalesce(sum(total) FILTER(WHERE status IN ('paid','refunded')),0) sales,
              sum(refund) refunds,coalesce(sum(total) FILTER(WHERE status IN ('paid','refunded')),0)-sum(refund) net,
              sum(paid_total) collected,sum(CASE WHEN status IN ('confirmed','partially_paid') THEN total-paid_total ELSE 0 END) receivable,
              CASE WHEN bool_or(missing) FILTER(WHERE status IN ('paid','refunded')) THEN NULL ELSE coalesce(sum(cost) FILTER(WHERE status IN ('paid','refunded')),0) END cost,
              coalesce(bool_or(missing) FILTER(WHERE status IN ('paid','refunded')),false) missing_cost
            FROM source GROUP BY business_date ORDER BY business_date
            """,ids,from,to);
        var collections=db.rows("SELECT p.method,p.kind,sum(p.amount) amount FROM payment p JOIN invoice i ON i.id=p.invoice_id WHERE i.branch_id=ANY(CAST(? AS uuid[])) AND i.business_date>=? AND i.business_date<? AND p.status='succeeded' AND p.kind IN ('collect','refund') AND NOT i.is_compensation GROUP BY p.method,p.kind ORDER BY p.method,p.kind",ids,from,to);
        Set<String> warnings=new LinkedHashSet<>();if(daily.isEmpty())warnings.add("EMPTY_PERIOD");
        for(var row:daily){if(Boolean.TRUE.equals(row.get("missing_cost")))warnings.add("MISSING_RECIPE_OR_COST_OR_CONSUMPTION");row.put("gross_profit",row.get("cost")==null?null:OperationRepository.money(row.get("net")).subtract(OperationRepository.money(row.get("cost"))));}
        var asOf=db.one("SELECT CURRENT_TIMESTAMP as_of").get("as_of");
        return Map.of("daily",serializable(daily),"collections",serializable(collections),"from",from,"to",to,
            "meta",Map.of("asOf",asOf,"generatedAt",asOf,"validUntil",((OffsetDateTime)asOf).plusSeconds(60),"scopeSummary",allowed,"metricVersion","SRS2-F08-v1","warnings",warnings,"interval","[from,to)","basis","Ngày kinh doanh của hóa đơn; hoàn tiền gắn về đơn gốc; tiền thu và còn phải thu báo riêng."));
    }
    private Object serializable(List<Map<String,Object>> rows){
        return rows.stream().map(row->{var result=new LinkedHashMap<String,Object>();row.forEach((k,v)->result.put(k,v instanceof BigDecimal n?n.toPlainString():v));return result;}).toList();
    }
}

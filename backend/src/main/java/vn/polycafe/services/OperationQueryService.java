package vn.polycafe.services;

import java.math.BigDecimal;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.polycafe.auth.StaffUser;
import vn.polycafe.common.*;
import vn.polycafe.dao.OperationRepository;
import static vn.polycafe.dao.OperationRepository.*;

@Service
public class OperationQueryService {
    private final OperationRepository db;private final AccessService access;private final PricingService pricing;private final OrderService orders;private final ShiftService shifts;private final AuditService audit;
    public OperationQueryService(OperationRepository db,AccessService access,PricingService pricing,OrderService orders,ShiftService shifts,AuditService audit){this.db=db;this.access=access;this.pricing=pricing;this.orders=orders;this.shifts=shifts;this.audit=audit;}
    public Object branches(StaffUser user){return access.branches(user);}
    public Object capabilities(UUID branch,StaffUser actor){var result=new ArrayList<String>();for(String action:List.of("order.read","order.create","order.confirm","prep.work","payment.collect","refund.request","refund.approve","ticket.work","ticket.reopen","shift.work","report.read","audit.read","staff.manage","stock.manage")){try{access.require(actor,branch,action);result.add(action);}catch(ApiException denied){if(denied.status!=403)throw denied;}}return result;}
    public Object references(UUID branch,String kind,String q,StaffUser actor){
        access.require(actor,branch,"order.read");
        return switch(kind){
            case "tables" -> db.rows("SELECT id,code,seats,status,zone_code FROM dining_table WHERE branch_id=? AND status<>'inactive' ORDER BY code",branch);
            case "pagers" -> db.rows("SELECT id,code,status FROM pager_device WHERE branch_id=? ORDER BY code",branch);
            case "stations" -> db.rows("SELECT id,name,kind,status FROM station WHERE branch_id=? ORDER BY name",branch);
            case "customers" -> db.rows("SELECT id,member_code,full_name,tier,points_balance FROM customer c WHERE kind='member' AND status='active' AND (? OR member_code=? OR phone=? OR lower(email)=lower(?) OR EXISTS(SELECT 1 FROM invoice i WHERE i.customer_id=c.id AND i.branch_id=?)) ORDER BY full_name,id LIMIT 100",actor.canManageProducts(),q,q,q,branch);
            default -> throw new ApiException(400,"Danh mục không hợp lệ.");
        };
    }
    @Transactional(readOnly=true)
    public Object menu(UUID branch,String channel,StaffUser actor){
        access.require(actor,branch,"menu.read");if(!Set.of("counter","table","takeaway").contains(channel))throw new ApiException(400,"Kênh bán không hợp lệ.");
        var result=new ArrayList<Map<String,Object>>();
        for(var row:db.rows("SELECT id FROM menu_item WHERE status='active' AND kind IN ('drink','food') AND (valid_from IS NULL OR valid_from<=CURRENT_TIMESTAMP) AND (valid_to IS NULL OR valid_to>CURRENT_TIMESTAMP) ORDER BY category_name,name LIMIT 200")){
            try{UUID item=id(row,"id");var quote=pricing.quote(branch,channel,new vn.polycafe.api.WorkflowRequests.Line(item,1,null,List.of(),null));var copy=new LinkedHashMap<>(quote.item());copy.remove("branch_overrides");copy.put("price",quote.price());copy.put("variants",db.rows("SELECT id,kind,name,extra_price,sort_order FROM menu_variant WHERE status='active' AND (item_id=? OR (item_id IS NULL AND kind<>'size')) ORDER BY kind,sort_order,id",item));result.add(copy);}catch(ApiException ex){if(ex.status!=404&&ex.status!=409)throw ex;}
        }
        return result;
    }
    public Object list(UUID branch,StaffUser actor){access.require(actor,branch,"order.read");return db.rows("SELECT i.id,i.invoice_no,i.channel,i.source,i.status,i.subtotal,i.discount_total,i.total,i.paid_total,i.created_at,i.business_date,t.code table_code,c.full_name customer_name FROM invoice i LEFT JOIN dining_table t ON t.id=i.table_id JOIN customer c ON c.id=i.customer_id WHERE i.branch_id=? ORDER BY i.created_at DESC,i.id LIMIT 100",branch);}
    @Transactional(readOnly=true)
    public Map<String,Object> detail(UUID invoice,StaffUser actor){
        var order=db.one("SELECT * FROM invoice WHERE id=?",invoice);UUID branch=id(order,"branch_id");access.require(actor,branch,"order.read");
        var lines=db.rows("SELECT l.*,m.name item_name,s.name station_name FROM invoice_line l LEFT JOIN menu_item m ON m.id=l.item_id LEFT JOIN station s ON s.id=l.station_id WHERE l.invoice_id=? ORDER BY l.created_at,l.id",invoice);
        BigDecimal quoted=BigDecimal.ZERO;String warning="";
        if("draft".equals(order.get("status"))){try{for(var line:lines)quoted=quoted.add(pricing.quote(branch,order.get("channel").toString(),orders.input(line)).price().multiply(money(line.get("quantity"))));}catch(ApiException ex){warning=ex.getMessage();}}
        else quoted=money(order.get("total"));
        var tickets=db.rows("SELECT t.*,p.code pager_code,p.status pager_status FROM ticket t LEFT JOIN pager_device p ON p.id=t.pager_id WHERE t.invoice_id=? ORDER BY t.issued_at,t.id",invoice);
        var payments=db.rows("SELECT id,kind,method,amount,status,tx_ref,paid_at,parent_payment_id,approval_id,confirm_note FROM payment WHERE invoice_id=? ORDER BY created_at,id",invoice);
        var approvals=db.rows("SELECT a.*,u.full_name requester_name FROM approval_request a JOIN payment p ON p.id=a.ref_id JOIN app_user u ON u.id=a.requested_by WHERE a.ref_type='payment' AND a.kind='refund' AND p.invoice_id=? ORDER BY a.created_at,a.id",invoice);
        BigDecimal refunded=money(db.one("SELECT coalesce(sum(amount),0) amount FROM payment WHERE invoice_id=? AND kind='refund' AND status='succeeded'",invoice).get("amount"));
        return Map.of("invoice",order,"lines",lines,"tickets",tickets,"payments",payments,"approvals",approvals,"quotedTotal",quoted,"quoteWarning",warning,"refunded",refunded);
    }
    public Object shiftList(UUID branch,StaffUser actor){
        access.require(actor,branch,"shift.work");boolean manage=false;try{access.require(actor,branch,"staff.manage");manage=true;}catch(ApiException ignored){}
        var result=db.rows("SELECT s.*,u.full_name operator_name FROM shift s JOIN app_user u ON u.id=s.opened_by WHERE branch_id=? AND (? OR opened_by=?) ORDER BY opened_at DESC,id LIMIT 50",branch,manage,actor.id());
        for(var shift:result)if("open".equals(shift.get("status")))shift.put("expected_cash",shifts.expected(shift));return result;
    }
    @Transactional
    public Object audit(UUID branch,UUID invoice,StaffUser actor){
        access.require(actor,branch,"audit.read");if(invoice!=null){var i=db.one("SELECT branch_id FROM invoice WHERE id=?",invoice);if(!branch.equals(id(i,"branch_id")))throw new ApiException(404,"Không tìm thấy đơn trong chi nhánh.");}
        var rows=db.rows("""
            SELECT a.id,a.actor_id,u.full_name actor_name,a.device_id,a.action,a.entity_type,a.entity_id,a.before_data,a.after_data,a.reason,a.request_id,a.created_at
            FROM audit_log a LEFT JOIN app_user u ON u.id=a.actor_id
            WHERE a.branch_id=? AND (CAST(? AS uuid) IS NULL OR a.entity_id=? OR
              (a.entity_type='invoice_line' AND a.entity_id IN (SELECT id FROM invoice_line WHERE invoice_id=?)) OR
              (a.entity_type='ticket' AND a.entity_id IN (SELECT id FROM ticket WHERE invoice_id=?)) OR
              (a.entity_type='approval_request' AND a.entity_id IN (SELECT a2.id FROM approval_request a2 JOIN payment p ON p.id=a2.ref_id WHERE a2.ref_type='payment' AND p.invoice_id=?)))
            ORDER BY a.created_at,a.id LIMIT 300
            """,branch,invoice,invoice,invoice,invoice,invoice);
        Object result=audit.redact(rows);audit.detail(actor.id(),branch,"audit.read","audit_log",invoice==null?UUID.randomUUID():invoice,null,Map.of("rows",rows.size()),"Tra cứu nhật ký");return result;
    }
}

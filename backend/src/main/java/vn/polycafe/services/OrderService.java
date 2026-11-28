package vn.polycafe.services;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import vn.polycafe.api.WorkflowRequests.*;
import vn.polycafe.auth.StaffUser;
import vn.polycafe.common.*;
import vn.polycafe.dao.OperationRepository;
import static vn.polycafe.dao.OperationRepository.*;

@Service
public class OrderService {
    private final OperationRepository db;private final AccessService access;private final CommandService commands;
    private final PricingService pricing;private final InventoryService inventory;private final AuditService audit;
    public OrderService(OperationRepository db,AccessService access,CommandService commands,PricingService pricing,InventoryService inventory,AuditService audit){this.db=db;this.access=access;this.commands=commands;this.pricing=pricing;this.inventory=inventory;this.audit=audit;}
    public LocalDate businessDate(UUID branch){var b=db.one("SELECT timezone,business_day_cutoff FROM branch WHERE id=?",branch);var now=ZonedDateTime.now(ZoneId.of(b.get("timezone").toString()));return now.toLocalTime().isBefore(LocalTime.parse(b.get("business_day_cutoff").toString()))?now.toLocalDate().minusDays(1):now.toLocalDate();}
    public Map<String,Object> quote(NewOrder request,StaffUser actor){access.require(actor,request.branchId(),"order.create");BigDecimal total=BigDecimal.ZERO;var lines=new ArrayList<Map<String,Object>>();for(Line line:request.lines()){var q=pricing.quote(request.branchId(),request.channel(),line);BigDecimal amount=q.price().multiply(BigDecimal.valueOf(line.quantity()));total=total.add(amount);lines.add(Map.of("itemId",line.itemId(),"unitPrice",q.price(),"amount",amount));}if(total.compareTo(new BigDecimal("99999999999999"))>0)throw new ApiException(400,"Tổng đơn vượt giới hạn lưu trữ.");return Map.of("total",total,"lines",lines);}
    public UUID session(UUID branch,UUID table){
        var t=db.lock("dining_table",table);if(!branch.equals(id(t,"branch_id"))||!Set.of("free","occupied").contains(t.get("status")))throw new ApiException(409,"Bàn không sẵn sàng hoặc khác chi nhánh.");
        var rows=db.rows("SELECT id FROM dining_session WHERE table_id=? AND status='open'",table);UUID session=rows.isEmpty()?UUID.randomUUID():id(rows.get(0),"id");
        if(rows.isEmpty())db.write("INSERT INTO dining_session(id,branch_id,table_id,status,opened_at) VALUES(?,?,?,'open',CURRENT_TIMESTAMP)",session,branch,table);
        db.write("UPDATE dining_table SET status='occupied' WHERE id=?",table);return session;
    }
    public Map<String,Object> create(NewOrder request,StaffUser actor){
        access.require(actor,request.branchId(),"order.create");
        return commands.execute(actor,request.branchId(),"order.create",request.requestId(),request,()->{
            UUID shift=access.openShift(actor,request.branchId()),customer=request.customerId();
            if(customer==null){customer=UUID.randomUUID();db.write("INSERT INTO customer(id,member_code,kind,status) VALUES(?,?,'guest','active')",customer,"G"+customer.toString().replace("-","").substring(0,18));}
            else db.one("SELECT id FROM customer WHERE id=? AND status='active'",customer);
            if(request.channel().equals("table") && request.tableId()==null)throw new ApiException(400,"Đơn tại bàn cần chọn bàn.");
            UUID table=request.tableId(),session=table==null?null:session(request.branchId(),table),invoice=UUID.randomUUID();
            BigDecimal total=BigDecimal.ZERO;var quotes=new ArrayList<PricingService.Quote>();
            for(Line line:request.lines()){var quote=pricing.quote(request.branchId(),request.channel(),line);quotes.add(quote);total=total.add(quote.price().multiply(BigDecimal.valueOf(line.quantity())));}
            db.write("INSERT INTO invoice(id,branch_id,invoice_no,table_id,session_id,source,staff_id,customer_id,channel,status,subtotal,discount_total,total,paid_total,idempotency_key,business_date,created_shift_id,responsible_shift_id,discount_snapshot) VALUES(?,?,?,?,?,'staff',?,?,?,'draft',?,0,?,0,?,?,?,?,CAST(? AS jsonb))",invoice,request.branchId(),"HD"+invoice.toString().replace("-","").substring(0,18),table,session,actor.id(),customer,request.channel(),total,total,"pos:"+invoice,businessDate(request.branchId()),shift,shift,"{\"schema_version\":1}");
            for(int i=0;i<request.lines().size();i++){Line line=request.lines().get(i);db.write("INSERT INTO invoice_line(invoice_id,item_id,size_id,quantity,unit_price,options_snapshot,note,status,line_kind) VALUES(?,?,?,?,?,CAST(? AS jsonb),?,'pending_accept','sale')",invoice,line.itemId(),line.sizeId(),line.quantity(),quotes.get(i).price(),db.json(Map.of("schema_version",1,"option_ids",line.optionIds()==null?List.of():line.optionIds())),line.note());}
            var result=db.lock("invoice",invoice);audit.detail(actor.id(),request.branchId(),"order.create","invoice",invoice,null,result,null);return result;
        });
    }
    @SuppressWarnings("unchecked")
    public Line input(Map<String,Object> line){
        List<UUID> options=new ArrayList<>();if(line.get("options_snapshot") instanceof Map<?,?> snapshot && snapshot.get("option_ids") instanceof List<?> ids)for(Object id:ids)options.add(UUID.fromString(id.toString()));
        return new Line(id(line,"item_id"),((Number)line.get("quantity")).intValue(),id(line,"size_id"),options,Objects.toString(line.get("note"),null));
    }
    public Map<String,Object> confirm(UUID invoice,Confirmation request,StaffUser actor){
        var read=db.one("SELECT branch_id FROM invoice WHERE id=?",invoice);UUID branch=id(read,"branch_id");access.require(actor,branch,"order.confirm");
        return commands.execute(actor,branch,"order.confirm:"+invoice,request.requestId(),request,()->{
            var before=db.lock("invoice",invoice);if(!"draft".equals(before.get("status")))throw new ApiException(409,"Đơn không còn ở trạng thái chờ xác nhận.");
            UUID shift=access.openShift(actor,branch);var lines=db.rows("SELECT * FROM invoice_line WHERE invoice_id=? ORDER BY id FOR UPDATE",invoice);if(lines.isEmpty())throw new ApiException(409,"Đơn chưa có món.");
            BigDecimal total=BigDecimal.ZERO;var quotes=new ArrayList<PricingService.Quote>();
            for(var line:lines){if(!"pending_accept".equals(line.get("status"))||!"sale".equals(line.get("line_kind")))throw new ApiException(409,"Dòng món không hợp lệ để xác nhận.");var quote=pricing.quote(branch,before.get("channel").toString(),input(line));quotes.add(quote);total=total.add(quote.price().multiply(money(line.get("quantity"))));}
            if(total.compareTo(request.expectedTotal())!=0)throw new ApiException(409,"Giá đã thay đổi. Tải lại đơn để xác nhận tổng mới.");
            for(int i=0;i<lines.size();i++){
                var line=lines.get(i);var quote=quotes.get(i);var stations=db.rows("SELECT id FROM station WHERE branch_id=? AND status='active' AND kind=? ORDER BY id",branch,quote.item().get("station_kind"));
                if(stations.isEmpty())throw new ApiException(409,"Chưa có quầy phục vụ loại món này.");
                var b=db.one("SELECT multi_station FROM branch WHERE id=?",branch);if(Boolean.TRUE.equals(b.get("multi_station"))&&stations.size()>1)throw new ApiException(409,"Có nhiều quầy cùng loại; cần chốt cấu hình quầy trước khi bán.");
                var snapshot=inventory.snapshot(before,line,quote,actor.id());db.write("UPDATE invoice_line SET unit_price=?,options_snapshot=CAST(? AS jsonb),status='waiting',station_id=? WHERE id=?",quote.price(),db.json(snapshot),stations.get(0).get("id"),line.get("id"));
            }
            db.write("UPDATE invoice SET status='confirmed',subtotal=?,total=?,discount_total=0,confirmed_by=?,confirmed_at=CURRENT_TIMESTAMP,responsible_shift_id=? WHERE id=?",total,total,actor.id(),shift,invoice);
            UUID ticket=UUID.randomUUID();db.write("INSERT INTO ticket(id,branch_id,invoice_id,ticket_no,status,issued_by,business_date,issued_at) VALUES(?,?,?,?,'open',?,?,CURRENT_TIMESTAMP)",ticket,branch,invoice,"T"+ticket.toString().replace("-","").substring(0,18),actor.id(),before.get("business_date"));
            var result=db.lock("invoice",invoice);audit.detail(actor.id(),branch,"order.confirm","invoice",invoice,before,result,"Xác nhận giá và giữ nguyên liệu");return result;
        });
    }
    public Map<String,Object> line(UUID lineId,String action,Command request,StaffUser actor){
        var found=db.one("SELECT i.id,i.branch_id FROM invoice i JOIN invoice_line l ON l.invoice_id=i.id WHERE l.id=?",lineId);UUID invoice=id(found,"id"),branch=id(found,"branch_id");
        access.require(actor,branch,action.equals("cancel")?"order.confirm":"prep.work");
        return commands.execute(actor,branch,"line."+action+":"+lineId,request.requestId(),request,()->{
            var order=db.lock("invoice",invoice);var before=db.lock("invoice_line",lineId);
            if(Set.of("cancelled","merged","draft").contains(order.get("status")))throw new ApiException(409,"Đơn chưa xác nhận hoặc đã hủy.");
            String status=before.get("status").toString();
            switch(action){
                case "accept" -> {access.station(actor,branch,id(before,"station_id"));if(!status.equals("waiting")||before.get("accepted_by")!=null)throw new ApiException(409,"Món đã được nhận hoặc chưa sẵn sàng.");db.write("UPDATE invoice_line SET accepted_by=?,accepted_at=CURRENT_TIMESTAMP WHERE id=?",actor.id(),lineId);}
                case "start" -> {access.station(actor,branch,id(before,"station_id"));if(!status.equals("waiting")||before.get("started_at")!=null||!actor.id().equals(id(before,"accepted_by")))throw new ApiException(409,"Chỉ người đã nhận món được bắt đầu một lần.");inventory.consume(order,before,actor.id());db.write("UPDATE invoice_line SET status='in_progress',prepared_by=?,started_at=CURRENT_TIMESTAMP WHERE id=?",actor.id(),lineId);}
                case "ready" -> {access.station(actor,branch,id(before,"station_id"));if(!status.equals("in_progress")||before.get("started_at")==null||!actor.id().equals(id(before,"prepared_by")))throw new ApiException(409,"Món chưa được chế biến bởi bạn.");db.write("UPDATE invoice_line SET status='ready',ready_by=?,ready_at=CURRENT_TIMESTAMP WHERE id=?",actor.id(),lineId);if(db.rows("SELECT id FROM invoice_line WHERE invoice_id=? AND status NOT IN ('ready','handed_over','canceled')",invoice).isEmpty())db.write("UPDATE ticket SET status='ready' WHERE invoice_id=? AND status='open'",invoice);}
                case "cancel" -> {if(!status.equals("waiting")||money(order.get("paid_total")).signum()>0||request.reason()==null||request.reason().isBlank())throw new ApiException(409,"Chỉ hủy món chưa làm, chưa thu tiền và phải ghi lý do. Món đã làm cần phê duyệt riêng.");inventory.release(order,before,actor.id());db.write("UPDATE invoice_line SET status='canceled' WHERE id=?",lineId);db.write("UPDATE invoice SET subtotal=(SELECT coalesce(sum(quantity*unit_price),0) FROM invoice_line WHERE invoice_id=? AND status<>'canceled'),total=(SELECT coalesce(sum(quantity*unit_price),0) FROM invoice_line WHERE invoice_id=? AND status<>'canceled') WHERE id=?",invoice,invoice,invoice);}
                default -> throw new ApiException(400,"Hành động không hợp lệ.");
            }
            var result=db.lock("invoice_line",lineId);audit.detail(actor.id(),branch,"line."+action,"invoice_line",lineId,before,result,request.reason());return result;
        });
    }
    public Map<String,Object> reject(UUID invoice,Command request,StaffUser actor){
        UUID branch=id(db.one("SELECT branch_id FROM invoice WHERE id=?",invoice),"branch_id");access.require(actor,branch,"order.confirm");
        return commands.execute(actor,branch,"order.reject:"+invoice,request.requestId(),request,()->{var before=db.lock("invoice",invoice);if(!"draft".equals(before.get("status"))||request.reason()==null||request.reason().isBlank())throw new ApiException(409,"Chỉ từ chối đơn nháp và cần lý do.");db.write("UPDATE invoice SET status='cancelled' WHERE id=?",invoice);db.write("UPDATE invoice_line SET status='canceled' WHERE invoice_id=?",invoice);var result=db.lock("invoice",invoice);audit.detail(actor.id(),branch,"order.reject","invoice",invoice,before,result,request.reason());releaseTable(invoice);return result;});
    }
    public void releaseTable(UUID invoice){
        var i=db.one("SELECT table_id,session_id FROM invoice WHERE id=?",invoice);UUID table=id(i,"table_id"),session=id(i,"session_id");if(table==null||session==null)return;
        db.lock("dining_table",table);
        var open=db.rows("SELECT id FROM invoice WHERE session_id=? AND status NOT IN ('cancelled','merged') AND (status NOT IN ('paid','refunded') OR EXISTS(SELECT 1 FROM invoice_line l WHERE l.invoice_id=invoice.id AND l.status NOT IN ('handed_over','canceled')))",session);
        if(open.isEmpty()){db.write("UPDATE dining_session SET status='closed',closed_at=CURRENT_TIMESTAMP WHERE id=? AND status='open'",session);db.write("UPDATE dining_table SET status='free' WHERE id=?",table);}
    }
}

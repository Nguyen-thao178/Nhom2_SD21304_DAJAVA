package vn.polycafe.services;

import java.util.*;
import org.springframework.stereotype.Service;
import vn.polycafe.api.WorkflowRequests.*;
import vn.polycafe.auth.StaffUser;
import vn.polycafe.common.*;
import vn.polycafe.dao.OperationRepository;
import static vn.polycafe.dao.OperationRepository.*;

@Service
public class TicketService {
    private final OperationRepository db;private final AccessService access;private final CommandService commands;private final AuditService audit;private final OrderService orders;
    public TicketService(OperationRepository db,AccessService access,CommandService commands,AuditService audit,OrderService orders){this.db=db;this.access=access;this.commands=commands;this.audit=audit;this.orders=orders;}
    private Map<String,Object> context(UUID ticket,StaffUser actor){var row=db.one("SELECT id,invoice_id,branch_id FROM ticket WHERE id=?",ticket);access.require(actor,id(row,"branch_id"),"ticket.work");return row;}
    public Map<String,Object> assign(UUID ticket,AssignPager request,StaffUser actor){
        var ctx=context(ticket,actor);UUID branch=id(ctx,"branch_id");
        return commands.execute(actor,branch,"ticket.assign:"+ticket,request.requestId(),request,()->{
            db.lock("invoice",id(ctx,"invoice_id"));var before=db.lock("ticket",ticket);if("closed".equals(before.get("status"))||before.get("pager_id")!=null)throw new ApiException(409,"Ticket đã đóng hoặc đang có thiết bị.");
            var pager=db.lock("pager_device",request.pagerId());if(!branch.equals(id(pager,"branch_id"))||!Set.of("ready","returned").contains(pager.get("status")))throw new ApiException(409,"Thiết bị không sẵn sàng tại chi nhánh.");
            if(!db.rows("SELECT id FROM ticket WHERE pager_id=? AND returned_at IS NULL",request.pagerId()).isEmpty())throw new ApiException(409,"Thiết bị đã cấp cho ticket khác.");
            db.write("UPDATE ticket SET pager_id=?,returned_at=NULL WHERE id=?",request.pagerId(),ticket);db.write("UPDATE pager_device SET status='issued' WHERE id=?",request.pagerId());
            event(before,null,request.pagerId(),"device_change",0,actor,null,request.requestId(),"Cấp thiết bị thủ công");var result=db.lock("ticket",ticket);audit.detail(actor.id(),branch,"ticket.assign","ticket",ticket,before,result,"Cấp thiết bị");return result;
        });
    }
    public Map<String,Object> handover(UUID ticket,Handover request,StaffUser actor){
        var ctx=context(ticket,actor);UUID branch=id(ctx,"branch_id"),invoice=id(ctx,"invoice_id");
        return commands.execute(actor,branch,"ticket.handover:"+ticket,request.requestId(),request,()->{
            db.lock("invoice",invoice);var before=db.lock("ticket",ticket);if("closed".equals(before.get("status")))throw new ApiException(409,"Ticket đã đóng.");
            var line=db.lock("invoice_line",request.lineId());if(!invoice.equals(id(line,"invoice_id"))||!"ready".equals(line.get("status")))throw new ApiException(409,"Món không thuộc ticket hoặc chưa sẵn sàng.");
            int next=((Number)line.get("handed_qty")).intValue()+request.quantity();if(next>((Number)line.get("quantity")).intValue())throw new ApiException(409,"Giao vượt số món còn lại.");
            db.write("UPDATE invoice_line SET handed_qty=?,status=? WHERE id=?",next,next==((Number)line.get("quantity")).intValue()?"handed_over":"ready",request.lineId());
            event(before,request.lineId(),id(before,"pager_id"),"customer_handover",request.quantity(),actor,request.receiver(),request.requestId(),request.note());
            var result=db.lock("invoice_line",request.lineId());audit.detail(actor.id(),branch,"ticket.handover","invoice_line",request.lineId(),line,result,request.note());orders.releaseTable(invoice);return result;
        });
    }
    public Map<String,Object> command(UUID ticket,String action,Command request,StaffUser actor){
        var ctx=context(ticket,actor);UUID branch=id(ctx,"branch_id"),invoice=id(ctx,"invoice_id");
        return commands.execute(actor,branch,"ticket."+action+":"+ticket,request.requestId(),request,()->{
            db.lock("invoice",invoice);var before=db.lock("ticket",ticket);UUID pagerId=id(before,"pager_id");
            switch(action){
                case "call" -> {
                    if("closed".equals(before.get("status"))||db.rows("SELECT id FROM invoice_line WHERE invoice_id=? AND status='ready' AND handed_qty<quantity",invoice).isEmpty())throw new ApiException(409,"Chưa có món sẵn sàng để gọi khách.");
                    if(pagerId!=null){var pager=db.lock("pager_device",pagerId);if(!Set.of("issued","calling").contains(pager.get("status")))throw new ApiException(409,"Thiết bị không ở trạng thái có thể gọi.");db.write("UPDATE pager_device SET status='calling' WHERE id=?",pagerId);}
                    event(before,null,pagerId,"call",0,actor,null,request.requestId(),"Gọi thủ công: "+Objects.toString(request.reason(),""));
                }
                case "return" -> {
                    if(pagerId==null||before.get("returned_at")!=null)throw new ApiException(409,"Không có thiết bị cần thu hồi.");
                    db.lock("pager_device",pagerId);db.write("UPDATE ticket SET returned_at=CURRENT_TIMESTAMP WHERE id=?",ticket);db.write("UPDATE pager_device SET status='ready' WHERE id=?",pagerId);event(before,null,pagerId,"return_pager",0,actor,null,request.requestId(),request.reason());
                }
                case "close" -> {
                    if("closed".equals(before.get("status")))throw new ApiException(409,"Ticket đã đóng.");
                    if(!db.rows("SELECT id FROM invoice_line WHERE invoice_id=? AND status<>'canceled' AND (status<>'handed_over' OR handed_qty<>quantity)",invoice).isEmpty())throw new ApiException(409,"Chưa giao đủ món; không thể đóng ticket.");
                    if(pagerId!=null&&before.get("returned_at")==null)throw new ApiException(409,"Hãy thu hồi thiết bị trước khi đóng ticket.");
                    db.write("UPDATE ticket SET status='closed',closed_at=CURRENT_TIMESTAMP WHERE id=?",ticket);orders.releaseTable(invoice);
                }
                case "reopen" -> {
                    access.require(actor,branch,"ticket.reopen");if(!"closed".equals(before.get("status"))||request.reason()==null||request.reason().isBlank())throw new ApiException(409,"Chỉ mở lại ticket đã đóng và phải ghi lý do.");
                    // Returned devices remain returned; reopening never borrows a device silently.
                    db.write("UPDATE ticket SET status='open',closed_at=NULL,reopened_count=reopened_count+1 WHERE id=?",ticket);
                }
                default -> throw new ApiException(400,"Hành động ticket không hợp lệ.");
            }
            var result=db.lock("ticket",ticket);audit.detail(actor.id(),branch,"ticket."+action,"ticket",ticket,before,result,request.reason());return result;
        });
    }
    private void event(Map<String,Object> ticket,UUID line,UUID pager,String kind,int quantity,StaffUser actor,String receiver,UUID request,String note){
        db.write("INSERT INTO fulfillment_event(invoice_id,line_id,ticket_id,pager_id,kind,quantity,actor_id,receiver_label,result,request_key,note) VALUES(?,?,?,?,?,?,?,?,'succeeded',?,?)",ticket.get("invoice_id"),line,ticket.get("id"),pager,kind,quantity,actor.id(),receiver,"wf:"+request,note);
    }
}

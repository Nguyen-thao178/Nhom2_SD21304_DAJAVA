package vn.polycafe.services;

import java.math.*;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vn.polycafe.api.WorkflowRequests.*;
import vn.polycafe.api.WorkflowRequests.Collection;
import vn.polycafe.auth.StaffUser;
import vn.polycafe.common.*;
import vn.polycafe.dao.OperationRepository;
import static vn.polycafe.dao.OperationRepository.*;

/** Manual confirmations only. No endpoint pretends to charge a bank or card. */
@Service
public class PaymentService {
    private final OperationRepository db;private final AccessService access;private final CommandService commands;private final AuditService audit;private final OrderService orders;
    private final BigDecimal pointUnit;private final int pointDays;
    public PaymentService(OperationRepository db,AccessService access,CommandService commands,AuditService audit,OrderService orders,
            @Value("${app.points.vnd-per-point:10000}") BigDecimal pointUnit,@Value("${app.points.valid-days:365}") int pointDays){this.db=db;this.access=access;this.commands=commands;this.audit=audit;this.orders=orders;if(pointUnit.signum()<=0||pointDays<1)throw new IllegalArgumentException("Invalid points policy");this.pointUnit=pointUnit;this.pointDays=pointDays;}
    private UUID branch(UUID invoice){return id(db.one("SELECT branch_id FROM invoice WHERE id=?",invoice),"branch_id");}
    private BigDecimal total(UUID invoice,String kind){return money(db.one("SELECT coalesce(sum(amount),0) amount FROM payment WHERE invoice_id=? AND kind=? AND status='succeeded'",invoice,kind).get("amount"));}
    private void uniqueReference(String ref){if(!db.rows("SELECT id FROM payment WHERE tx_ref=?",ref).isEmpty())throw new ApiException(409,"Mã giao dịch đã được ghi nhận. Hãy tra cứu, không thu hoặc hoàn lại lần nữa.");}
    public Map<String,Object> collect(UUID invoice,Collection request,StaffUser actor){
        UUID branch=branch(invoice);access.require(actor,branch,"payment.collect");
        return commands.execute(actor,branch,"payment.collect:"+invoice,request.requestId(),request,()->{
            var before=db.lock("invoice",invoice);if(!Set.of("confirmed","partially_paid").contains(before.get("status")))throw new ApiException(409,"Chỉ thu tiền cho đơn đã xác nhận và còn phải thu.");
            UUID shift=access.openShift(actor,branch);BigDecimal collected=total(invoice,"collect"),due=money(before.get("total")).subtract(collected);
            if(request.amount().compareTo(due)>0)throw new ApiException(409,"Khoản thu vượt số còn phải thu. Chỉ ghi phần giữ lại, trả tiền thừa cho khách.");
            if(!request.method().equals("cash")&&(request.note()==null||request.note().isBlank()))throw new ApiException(400,"Cần ghi thông tin đối chiếu cho khoản thu không tiền mặt.");
            uniqueReference(request.txRef());UUID payment=UUID.randomUUID();
            db.write("INSERT INTO payment(id,invoice_id,shift_id,method,amount,status,tx_ref,confirmed_by,confirm_note,paid_at,kind) VALUES(?,?,?,?,?,'succeeded',?,?,?,CURRENT_TIMESTAMP,'collect')",payment,invoice,shift,request.method(),request.amount(),request.txRef(),actor.id(),request.note());
            BigDecimal next=collected.add(request.amount());boolean paid=next.compareTo(money(before.get("total")))==0;
            db.write("UPDATE invoice SET paid_total=?,status=? WHERE id=?",next,paid?"paid":"partially_paid",invoice);
            if(paid){earn(before);orders.releaseTable(invoice);}
            var result=db.lock("invoice",invoice);audit.detail(actor.id(),branch,"payment.collect","invoice",invoice,before,result,request.note());
            return Map.of("invoice",result,"payment",db.lock("payment",payment));
        });
    }
    private void earn(Map<String,Object> invoice){
        UUID customer=id(invoice,"customer_id"),invoiceId=id(invoice,"id");var c=db.lock("customer",customer);
        if(!"member".equals(c.get("kind"))||!"active".equals(c.get("status"))||Boolean.TRUE.equals(invoice.get("is_compensation")))return;
        int points=money(invoice.get("total")).divide(pointUnit,0,RoundingMode.DOWN).intValueExact();if(points==0)return;
        if(!db.rows("SELECT id FROM point_ledger WHERE invoice_id=? AND kind='earn'",invoiceId).isEmpty())throw new ApiException(409,"Hóa đơn đã có điểm thưởng; cần kiểm tra đối chiếu.");
        int next=Math.addExact(((Number)c.get("points_balance")).intValue(),points);
        db.write("INSERT INTO point_ledger(customer_id,invoice_id,kind,points,balance_after,expires_at) VALUES(?,?,'earn',?,?,CURRENT_TIMESTAMP+(? * interval '1 day'))",customer,invoiceId,points,next,pointDays);
        db.write("UPDATE customer SET points_balance=? WHERE id=?",next,customer);
    }
    public Map<String,Object> requestRefund(UUID invoice,RefundRequest request,StaffUser actor){
        UUID branch=branch(invoice);access.require(actor,branch,"refund.request");
        return commands.execute(actor,branch,"refund.request:"+invoice,request.requestId(),request,()->{
            var order=db.lock("invoice",invoice);var payment=db.lock("payment",request.paymentId());
            if(!invoice.equals(id(payment,"invoice_id"))||!"collect".equals(payment.get("kind"))||!"succeeded".equals(payment.get("status"))||!Set.of("paid","refunded").contains(order.get("status")))throw new ApiException(409,"Cần giao dịch thu thành công của hóa đơn đã thu đủ.");
            BigDecimal used=money(db.one("SELECT coalesce(sum(amount),0) amount FROM payment WHERE parent_payment_id=? AND kind='refund' AND status IN ('pending','succeeded','pending_reconcile')",request.paymentId()).get("amount"));
            BigDecimal reserved=money(db.one("SELECT coalesce(sum(a.amount),0) amount FROM approval_request a WHERE a.kind='refund' AND a.ref_type='payment' AND a.ref_id=? AND a.status IN ('pending','approved') AND NOT EXISTS(SELECT 1 FROM payment p WHERE p.approval_id=a.id AND p.status IN ('pending','succeeded','pending_reconcile'))",request.paymentId()).get("amount"));
            if(used.add(reserved).add(request.amount()).compareTo(money(payment.get("amount")))>0)throw new ApiException(409,"Số tiền hoàn và các yêu cầu đang giữ vượt khoản thu gốc.");
            UUID approval=UUID.randomUUID();db.write("INSERT INTO approval_request(id,kind,ref_type,ref_id,amount,reason,requested_by,status) VALUES(?,'refund','payment',?,?,?,?,'pending')",approval,request.paymentId(),request.amount(),request.reason(),actor.id());
            var result=db.lock("approval_request",approval);audit.detail(actor.id(),branch,"refund.request","approval_request",approval,null,result,request.reason());return result;
        });
    }
    private Map<String,Object> context(UUID approval){return db.one("SELECT p.invoice_id,i.branch_id FROM approval_request a JOIN payment p ON p.id=a.ref_id JOIN invoice i ON i.id=p.invoice_id WHERE a.id=? AND a.kind='refund' AND a.ref_type='payment'",approval);}
    public Map<String,Object> decide(UUID approval,Decision request,StaffUser actor){
        var context=context(approval);UUID branch=id(context,"branch_id"),invoice=id(context,"invoice_id");BigDecimal limit=access.require(actor,branch,"refund.approve");
        return commands.execute(actor,branch,"approval.decide:"+approval,request.requestId(),request,()->{
            db.lock("invoice",invoice);var before=db.lock("approval_request",approval);
            if(actor.id().equals(id(before,"requested_by")))throw new ApiException(403,"Không được tự duyệt yêu cầu của mình.");
            if(!"pending".equals(before.get("status")))throw new ApiException(409,"Yêu cầu đã được quyết định.");
            if(money(before.get("amount")).compareTo(limit)>0)throw new ApiException(403,"Số tiền vượt hạn mức duyệt được cấp.");
            db.write("UPDATE approval_request SET status=?,approver_id=?,decided_at=CURRENT_TIMESTAMP WHERE id=?",request.approve()?"approved":"rejected",actor.id(),approval);
            var result=db.lock("approval_request",approval);audit.detail(actor.id(),branch,"approval.decide","approval_request",approval,before,result,request.reason());return result;
        });
    }
    public Map<String,Object> refund(UUID invoice,Refund request,StaffUser actor){
        UUID branch=branch(invoice);access.require(actor,branch,"payment.collect");
        return commands.execute(actor,branch,"payment.refund:"+invoice,request.requestId(),request,()->{
            var order=db.lock("invoice",invoice);var ctx=context(request.approvalId());if(!invoice.equals(id(ctx,"invoice_id")))throw new ApiException(404,"Yêu cầu hoàn không thuộc hóa đơn.");
            var approval=db.lock("approval_request",request.approvalId());var parent=db.lock("payment",id(approval,"ref_id"));
            if(!"approved".equals(approval.get("status"))||!"succeeded".equals(parent.get("status"))||!"collect".equals(parent.get("kind")))throw new ApiException(409,"Chưa có phê duyệt hoàn tiền hợp lệ.");
            if(!db.rows("SELECT id FROM payment WHERE approval_id=? AND status IN ('pending','succeeded','pending_reconcile')",request.approvalId()).isEmpty())throw new ApiException(409,"Phê duyệt này đã được thực hiện.");
            BigDecimal amount=money(approval.get("amount")),used=money(db.one("SELECT coalesce(sum(amount),0) amount FROM payment WHERE parent_payment_id=? AND kind='refund' AND status IN ('pending','succeeded','pending_reconcile')",parent.get("id")).get("amount"));
            if(used.add(amount).compareTo(money(parent.get("amount")))>0)throw new ApiException(409,"Hoàn vượt giao dịch gốc.");
            UUID shift=access.openShift(actor,branch);uniqueReference(request.txRef());UUID payment=UUID.randomUUID();
            db.write("INSERT INTO payment(id,invoice_id,parent_payment_id,approval_id,shift_id,method,amount,status,tx_ref,requested_by,reason,confirmed_by,confirm_note,paid_at,kind) VALUES(?,?,?,?,?,?,?,'succeeded',?,?,?,?,?,CURRENT_TIMESTAMP,'refund')",payment,invoice,parent.get("id"),request.approvalId(),shift,parent.get("method"),amount,request.txRef(),approval.get("requested_by"),approval.get("reason"),actor.id(),request.note());
            BigDecimal refunds=total(invoice,"refund");reversePoints(order,refunds);
            if(refunds.compareTo(total(invoice,"collect"))==0)db.write("UPDATE invoice SET status='refunded' WHERE id=?",invoice);
            var result=db.lock("invoice",invoice);audit.detail(actor.id(),branch,"payment.refund","invoice",invoice,order,result,request.note());return Map.of("invoice",result,"payment",db.lock("payment",payment));
        });
    }
    private void reversePoints(Map<String,Object> invoice,BigDecimal refunded){
        var earns=db.rows("SELECT * FROM point_ledger WHERE invoice_id=? AND kind='earn' ORDER BY created_at,id",invoice.get("id"));if(earns.isEmpty())return;
        var c=db.lock("customer",id(invoice,"customer_id"));int balance=((Number)c.get("points_balance")).intValue();
        for(var earn:earns){int original=((Number)earn.get("points")).intValue();int target=BigDecimal.valueOf(original).multiply(refunded).divide(money(invoice.get("total")),0,RoundingMode.DOWN).intValueExact();
            int reversed=money(db.one("SELECT -coalesce(sum(points),0) points FROM point_ledger WHERE origin_entry_id=? AND kind='reverse'",earn.get("id")).get("points")).intValueExact();int delta=target-reversed;
            if(delta<=0)continue;if(delta>balance)throw new ApiException(409,"Điểm đã sử dụng không đủ để đảo. Cần quản lý xử lý điểm trước khi hoàn.");balance-=delta;
            db.write("INSERT INTO point_ledger(customer_id,invoice_id,kind,points,balance_after,origin_entry_id) VALUES(?,?,'reverse',?,?,?)",c.get("id"),invoice.get("id"),-delta,balance,earn.get("id"));
        }
        db.write("UPDATE customer SET points_balance=? WHERE id=?",balance,c.get("id"));
    }
}

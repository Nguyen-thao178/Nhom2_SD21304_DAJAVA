package vn.polycafe.services;

import java.math.BigDecimal;
import java.util.*;
import org.springframework.stereotype.Service;
import vn.polycafe.api.WorkflowRequests.*;
import vn.polycafe.auth.StaffUser;
import vn.polycafe.common.*;
import vn.polycafe.dao.OperationRepository;
import static vn.polycafe.dao.OperationRepository.*;

@Service
public class ShiftService {
    private final OperationRepository db;private final AccessService access;private final CommandService commands;private final AuditService audit;
    public ShiftService(OperationRepository db,AccessService access,CommandService commands,AuditService audit){this.db=db;this.access=access;this.commands=commands;this.audit=audit;}
    public Map<String,Object> open(OpenShift request,StaffUser actor){
        access.require(actor,request.branchId(),"shift.work");
        return commands.execute(actor,request.branchId(),"shift.open",request.requestId(),request,()->{
            db.advisory("shift-open:"+actor.id());if(!db.rows("SELECT id FROM shift WHERE opened_by=? AND status='open'",actor.id()).isEmpty())throw new ApiException(409,"Bạn đang có ca mở. Hãy kết ca trước.");
            UUID shift=UUID.randomUUID();db.write("INSERT INTO shift(id,branch_id,opened_by,opened_at,opening_cash,status) VALUES(?,?,?,CURRENT_TIMESTAMP,?,'open')",shift,request.branchId(),actor.id(),request.openingCash());
            db.write("INSERT INTO shift_assignment(shift_id,user_id,from_at,duty) VALUES(?,?,CURRENT_TIMESTAMP,'Thu ngân kiêm phục vụ')",shift,actor.id());
            var result=db.lock("shift",shift);audit.detail(actor.id(),request.branchId(),"shift.open","shift",shift,null,result,null);return result;
        });
    }
    public BigDecimal expected(Map<String,Object> shift){
        BigDecimal cash=money(db.one("SELECT coalesce(sum(CASE WHEN kind='collect' THEN amount WHEN kind='refund' THEN -amount ELSE 0 END),0) amount FROM payment WHERE shift_id=? AND method='cash' AND status='succeeded'",shift.get("id")).get("amount"));
        BigDecimal entries=money(db.one("SELECT coalesce(sum(CASE WHEN kind='in' THEN amount ELSE -amount END),0) amount FROM cash_entry WHERE shift_id=?",shift.get("id")).get("amount"));return money(shift.get("opening_cash")).add(cash).add(entries);
    }
    public Map<String,Object> close(UUID shiftId,CloseShift request,StaffUser actor){
        var info=db.one("SELECT * FROM shift WHERE id=?",shiftId);UUID branch=id(info,"branch_id");access.require(actor,branch,"shift.work");if(!actor.id().equals(id(info,"opened_by")))access.require(actor,branch,"staff.manage");
        return commands.execute(actor,branch,"shift.close:"+shiftId,request.requestId(),request,()->{
            // Same actor lock order as opening avoids two open/close requests racing.
            db.advisory("shift-open:"+info.get("opened_by"));var before=db.lock("shift",shiftId);if(!"open".equals(before.get("status")))throw new ApiException(409,"Ca đã đóng.");
            if(!db.rows("SELECT id FROM invoice WHERE responsible_shift_id=? AND status IN ('draft','confirmed','partially_paid') AND total>paid_total",shiftId).isEmpty())throw new ApiException(409,"Ca còn đơn chưa thu. Thu xong hoặc thực hiện bàn giao được quản lý xác nhận trước khi kết ca.");
            BigDecimal expected=expected(before),difference=request.countedCash().subtract(expected);
            if(difference.signum()!=0&&(actor.id().equals(id(before,"opened_by"))||request.reason()==null||request.reason().isBlank()))throw new ApiException(409,"Chênh lệch tiền cần quản lý khác người mở ca xác nhận và ghi lý do.");
            db.write("UPDATE shift SET expected_cash=?,counted_cash=?,difference=?,closed_at=CURRENT_TIMESTAMP,status='closed' WHERE id=?",expected,request.countedCash(),difference,shiftId);db.write("UPDATE shift_assignment SET to_at=CURRENT_TIMESTAMP WHERE shift_id=? AND to_at IS NULL",shiftId);
            var result=db.lock("shift",shiftId);audit.detail(actor.id(),branch,"shift.close","shift",shiftId,before,result,request.reason());return result;
        });
    }
}

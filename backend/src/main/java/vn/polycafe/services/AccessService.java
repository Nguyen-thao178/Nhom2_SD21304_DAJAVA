package vn.polycafe.services;

import java.math.BigDecimal;
import java.util.*;
import org.springframework.stereotype.Service;
import vn.polycafe.auth.StaffUser;
import vn.polycafe.common.ApiException;
import vn.polycafe.dao.OperationRepository;

@Service
public class AccessService {
    private final OperationRepository db;
    private static final Set<String> STAFF=Set.of("order.read","order.create","order.confirm","payment.collect","refund.request","ticket.work","shift.work","menu.read");
    private static final Set<String> ACCOUNTANT=Set.of("order.read","report.read","audit.read","payment.reconcile");
    public AccessService(OperationRepository db){this.db=db;}
    public List<Map<String,Object>> branches(StaffUser user){
        requireStaff(user);
        return db.rows("SELECT b.id,b.name,b.code FROM branch b WHERE b.status='active' AND (? OR EXISTS(SELECT 1 FROM user_access a WHERE a.user_id=? AND a.status='active' AND a.branch_id=b.id AND a.role IN ('staff','shift_lead','owner','sysadmin','accountant'))) ORDER BY b.name",user.canManageProducts(),user.id());
    }
    public void requireStaff(StaffUser u){if(u==null||u.roles().contains("customer"))throw new ApiException(403,"Chức năng dành cho nhân viên.");}
    @SuppressWarnings("unchecked")
    public BigDecimal require(StaffUser user,UUID branch,String action){
        requireStaff(user);
        db.one("SELECT id FROM branch WHERE id=? AND status='active'",branch);
        if(user.canManageProducts())return new BigDecimal("99999999999999");
        BigDecimal limit=BigDecimal.valueOf(-1);
        for(var row:db.rows("SELECT role,permissions,refund_limit,discount_limit_pct FROM user_access WHERE user_id=? AND branch_id=? AND status='active'",user.id(),branch)){
            String role=row.get("role").toString();boolean allowed=Set.of("owner","sysadmin","shift_lead").contains(role) || (role.equals("staff")&&STAFF.contains(action)) || (role.equals("accountant")&&ACCOUNTANT.contains(action));
            Object raw=row.get("permissions");if(raw instanceof Map<?,?> p && p.get("actions") instanceof List<?> actions)allowed|=actions.contains(action);
            if(action.equals("prep.work") && role.equals("staff"))allowed|=!db.rows("SELECT a.id FROM shift_assignment a JOIN shift s ON s.id=a.shift_id JOIN branch b ON b.id=s.branch_id WHERE a.user_id=? AND s.branch_id=? AND s.status='open' AND a.from_at<=CURRENT_TIMESTAMP AND (a.to_at IS NULL OR a.to_at>CURRENT_TIMESTAMP) AND NOT b.multi_station",user.id(),branch).isEmpty();
            if(allowed)limit=limit.max(action.equals("refund.approve")?OperationRepository.money(row.get("refund_limit")):new BigDecimal("99999999999999"));
        }
        if(limit.signum()<0)throw new ApiException(403,"Bạn chưa có quyền thao tác tại chi nhánh này.");return limit;
    }
    public void station(StaffUser user,UUID branch,UUID station){
        require(user,branch,"prep.work");if(user.canManageProducts())return;
        if(!db.rows("SELECT id FROM user_access WHERE user_id=? AND branch_id=? AND status='active' AND role IN ('owner','sysadmin','shift_lead')",user.id(),branch).isEmpty())return;
        var b=db.one("SELECT multi_station FROM branch WHERE id=?",branch);if(Boolean.FALSE.equals(b.get("multi_station")))return;
        if(db.rows("SELECT a.id FROM shift_assignment a JOIN shift s ON s.id=a.shift_id WHERE a.user_id=? AND a.station_id=? AND s.branch_id=? AND s.status='open' AND a.from_at<=CURRENT_TIMESTAMP AND (a.to_at IS NULL OR a.to_at>CURRENT_TIMESTAMP)",user.id(),station,branch).isEmpty())throw new ApiException(403,"Bạn chưa được phân công tại quầy này.");
    }
    public UUID openShift(StaffUser user,UUID branch){
        var rows=db.rows("SELECT id FROM shift WHERE branch_id=? AND opened_by=? AND status='open' ORDER BY opened_at DESC FOR UPDATE",branch,user.id());
        if(rows.size()!=1)throw new ApiException(409,"Hãy mở ca của bạn trước khi bán hoặc thu tiền.");return OperationRepository.id(rows.get(0),"id");
    }
}

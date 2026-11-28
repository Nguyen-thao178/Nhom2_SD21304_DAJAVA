package vn.polycafe.services;

import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import vn.polycafe.api.WorkflowRequests.StaffAccount;
import vn.polycafe.auth.StaffUser;
import vn.polycafe.common.*;
import vn.polycafe.dao.OperationRepository;
import static vn.polycafe.dao.OperationRepository.*;

@Service
public class StaffService {
    private final OperationRepository db;private final AccessService access;private final PasswordEncoder passwords;private final AuditService audit;
    public StaffService(OperationRepository db,AccessService access,PasswordEncoder passwords,AuditService audit){this.db=db;this.access=access;this.passwords=passwords;this.audit=audit;}
    public Object list(UUID branch,StaffUser actor){access.require(actor,branch,"staff.manage");return db.rows("SELECT DISTINCT u.id,u.username,u.full_name,u.status FROM app_user u JOIN user_access a ON a.user_id=u.id WHERE a.branch_id=? AND a.status='active' AND u.account_kind='staff' ORDER BY u.full_name",branch);}
    @Transactional public Object create(StaffAccount request,StaffUser actor){
        access.require(actor,request.branchId(),"staff.manage");
        if(request.password().getBytes(java.nio.charset.StandardCharsets.UTF_8).length>72)throw new ApiException(400,"Mật khẩu tối đa 72 byte UTF-8.");
        UUID id=UUID.randomUUID();db.write("INSERT INTO app_user(id,username,full_name,password_hash,status,account_kind) VALUES(?,?,?,?,'active','staff')",id,request.username(),request.fullName(),passwords.encode(request.password()));
        db.write("INSERT INTO user_access(user_id,branch_id,role,permissions,status) VALUES(?,?,'staff',CAST(? AS jsonb),'active')",id,request.branchId(),"{\"schema_version\":1}");
        var result=Map.of("id",id,"username",request.username(),"full_name",request.fullName(),"status","active");audit.detail(actor.id(),request.branchId(),"staff.create","app_user",id,null,result,"Cấp quyền nhân viên chi nhánh");return result;
    }
    @Transactional public Object status(UUID branch,UUID user,boolean locked,String reason,StaffUser actor){
        access.require(actor,branch,"staff.manage");if(actor.id().equals(user)||reason==null||reason.isBlank())throw new ApiException(403,"Không đổi trạng thái chính mình và phải ghi lý do.");
        db.advisory("identity-administration");var before=db.lock("app_user",user);
        if(!actor.canManageProducts()){
            if(db.rows("SELECT id FROM user_access WHERE user_id=? AND branch_id=? AND role='staff' AND status='active'",user,branch).isEmpty())throw new ApiException(403,"Chỉ quản lý nhân viên thường trong chi nhánh mình.");
            if(!db.rows("SELECT id FROM user_access WHERE user_id=? AND status='active' AND (branch_id IS DISTINCT FROM CAST(? AS uuid) OR role<>'staff')",user,branch).isEmpty())throw new ApiException(403,"Tài khoản có phạm vi khác; cần quản trị toàn hệ thống.");
        }
        if(locked&&!db.rows("SELECT id FROM user_access WHERE user_id=? AND branch_id IS NULL AND role IN ('owner','sysadmin') AND status='active'",user).isEmpty()){
            var admins=db.rows("SELECT DISTINCT u.id FROM app_user u JOIN user_access a ON a.user_id=u.id WHERE u.status='active' AND a.status='active' AND a.branch_id IS NULL AND a.role IN ('owner','sysadmin')");if(admins.size()<=1)throw new ApiException(409,"Không được khóa quản trị viên cuối cùng.");
        }
        db.write("UPDATE app_user SET status=?,failed_count=0,locked_until=NULL,refresh_version=refresh_version+1 WHERE id=?",locked?"locked":"active",user);
        var result=Map.of("id",user,"status",locked?"locked":"active");audit.detail(actor.id(),branch,"staff.status","app_user",user,Map.of("status",before.get("status")),result,reason);return result;
    }
}

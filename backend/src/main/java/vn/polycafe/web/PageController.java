package vn.polycafe.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import vn.polycafe.auth.StaffUser;
import vn.polycafe.crud.SchemaCatalog;
import java.util.*;

@Controller
public class PageController {
    public record Screen(String path,String title,String subtitle,String group,String resource) {
        public String getPath(){return path;} public String getTitle(){return title;}
        public String getSubtitle(){return subtitle;} public String getGroup(){return group;}
        public String getResource(){return resource;}
    }
    private static final List<Screen> SCREENS=List.of(
        new Screen("overview","Tổng quan","Một góc nhìn chung cho ngày vận hành.","Vận hành",""),
        new Screen("pos","Bán hàng","Chọn món và chuẩn bị đơn tại quầy.","Vận hành",""),
        new Screen("orders","Đơn hàng","Theo dõi đơn và chỉnh sửa các bản nháp.","Vận hành","invoice"),
        new Screen("tables","Sơ đồ bàn","Tình trạng bàn và phiên phục vụ.","Vận hành","dining_table"),
        new Screen("kitchen","Quầy chế biến","Món chờ làm, đang làm và sẵn sàng bàn giao.","Vận hành","invoice_line"),
        new Screen("tickets","Ticket & nhận món","Theo dõi lượt nhận và thiết bị rung.","Vận hành","ticket"),
        new Screen("menu","Thực đơn","Món bán, giá chuẩn và trạng thái phục vụ.","Quản lý",""),
        new Screen("inventory","Nguyên liệu & kho","Theo dõi nguyên liệu, lô hàng và chứng từ.","Quản lý","ingredient"),
        new Screen("purchasing","Mua hàng","Đơn mua và các đợt nhận nguyên liệu.","Quản lý","purchase_order"),
        new Screen("customers","Khách hàng","Hồ sơ thành viên và lịch sử điểm.","Quản lý","customer"),
        new Screen("shifts","Nhân viên & ca","Phân công và bàn giao trong ngày.","Quản lý","shift"),
        new Screen("reports","Báo cáo","Doanh thu, thu tiền và đối soát theo kỳ.","Quản lý",""),
        new Screen("settings","Thiết lập","Chi nhánh, quyền truy cập và tích hợp.","Hệ thống","branch"));
    private final SchemaCatalog catalog;
    public PageController(SchemaCatalog catalog) {this.catalog=catalog;}
    @GetMapping({"/","/app"}) public String home() {return "redirect:/app/overview";}
    @GetMapping("/login") public String login(@AuthenticationPrincipal StaffUser user) {return user==null?"login":user.roles().contains("customer")?"redirect:/customer":"redirect:/app/overview";}
    @GetMapping({"/customer","/qr/{token}"}) public String customer() {return "customer";}
    @GetMapping("/app/qr-codes") public String qrCodes() {return "qr-codes";}
    @GetMapping("/app/{screen}")
    public String screen(@PathVariable String screen,@AuthenticationPrincipal StaffUser user,Model model) {
        Screen selected=SCREENS.stream().filter(s->s.path().equals(screen)).findFirst().orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));
        common(model,user,selected);
        return "app";
    }
    @GetMapping("/app/data/{table}")
    public String resource(@PathVariable String table,@AuthenticationPrincipal StaffUser user,Model model) {
        if(!user.canManageProducts()) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        catalog.table(table);
        common(model,user,new Screen("data",table.replace('_',' '),"Danh sách, chi tiết và các thao tác trong phạm vi được phép.","Hệ thống",table));
        return "app";
    }
    private void common(Model model,StaffUser user,Screen selected) {
        model.addAttribute("screens",SCREENS);model.addAttribute("page",selected);model.addAttribute("staff",user);
        model.addAttribute("staffName",user.fullName());model.addAttribute("staffUsername",user.username());
        model.addAttribute("isManager",user.canManageProducts());
        model.addAttribute("allTables",catalog.tables().stream().map(SchemaCatalog.Table::name).sorted().toList());
    }
}

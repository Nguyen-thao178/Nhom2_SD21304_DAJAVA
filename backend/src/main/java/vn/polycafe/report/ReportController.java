package vn.polycafe.report;

import java.time.LocalDate;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import vn.polycafe.auth.StaffUser;
import vn.polycafe.services.RevenueService;

@RestController @RequestMapping("/api/v1/reports/revenue")
public class ReportController {
    private final RevenueService revenue;
    public ReportController(RevenueService revenue){this.revenue=revenue;}
    @GetMapping Object report(@RequestParam LocalDate from,@RequestParam LocalDate to,@RequestParam(required=false) UUID branch,@AuthenticationPrincipal StaffUser actor){return revenue.report(from,to,branch,actor);}
}
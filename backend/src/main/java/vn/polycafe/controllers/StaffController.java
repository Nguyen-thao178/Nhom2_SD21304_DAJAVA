package vn.polycafe.controllers;

import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import vn.polycafe.api.WorkflowRequests.StaffAccount;
import vn.polycafe.auth.StaffUser;
import vn.polycafe.services.StaffService;

@RestController @RequestMapping("/api/v1/staff/users")
public class StaffController {
    public record Status(boolean locked,String reason) {}
    private final StaffService staff;
    public StaffController(StaffService staff){this.staff=staff;}
    @GetMapping Object list(@RequestParam UUID branch,@AuthenticationPrincipal StaffUser u){return staff.list(branch,u);}
    @PostMapping Object create(@Valid @RequestBody StaffAccount r,@AuthenticationPrincipal StaffUser u){return staff.create(r,u);}
    @PostMapping("/{id}/status") Object status(@PathVariable UUID id,@RequestParam UUID branch,@RequestBody Status r,@AuthenticationPrincipal StaffUser u){return staff.status(branch,id,r.locked(),r.reason(),u);}
}

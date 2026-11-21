package vn.polycafe.crud.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.polycafe.crud.BaseCrudController;
import vn.polycafe.crud.CrudService;

@RestController
@RequestMapping("/api/v1/audit-log")
public class AuditLogController extends BaseCrudController {
    public AuditLogController(CrudService service) { super("audit_log", service); }
}

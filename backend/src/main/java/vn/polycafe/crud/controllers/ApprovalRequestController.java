package vn.polycafe.crud.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.polycafe.crud.BaseCrudController;
import vn.polycafe.crud.CrudService;

@RestController
@RequestMapping("/api/v1/approval-request")
public class ApprovalRequestController extends BaseCrudController {
    public ApprovalRequestController(CrudService service) { super("approval_request", service); }
}

package vn.polycafe.crud.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.polycafe.crud.BaseCrudController;
import vn.polycafe.crud.CrudService;

@RestController
@RequestMapping("/api/v1/user-access")
public class UserAccessController extends BaseCrudController {
    public UserAccessController(CrudService service) { super("user_access", service); }
}

package vn.polycafe.crud.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.polycafe.crud.BaseCrudController;
import vn.polycafe.crud.CrudService;

@RestController
@RequestMapping("/api/v1/integration-config")
public class IntegrationConfigController extends BaseCrudController {
    public IntegrationConfigController(CrudService service) { super("integration_config", service); }
}

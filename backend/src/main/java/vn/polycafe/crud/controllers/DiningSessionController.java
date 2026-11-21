package vn.polycafe.crud.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.polycafe.crud.BaseCrudController;
import vn.polycafe.crud.CrudService;

@RestController
@RequestMapping("/api/v1/dining-session")
public class DiningSessionController extends BaseCrudController {
    public DiningSessionController(CrudService service) { super("dining_session", service); }
}

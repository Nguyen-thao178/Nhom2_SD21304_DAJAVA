package vn.polycafe.crud.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.polycafe.crud.BaseCrudController;
import vn.polycafe.crud.CrudService;

@RestController
@RequestMapping("/api/v1/dining-table")
public class DiningTableController extends BaseCrudController {
    public DiningTableController(CrudService service) { super("dining_table", service); }
}

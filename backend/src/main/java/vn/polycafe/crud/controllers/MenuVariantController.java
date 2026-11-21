package vn.polycafe.crud.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.polycafe.crud.BaseCrudController;
import vn.polycafe.crud.CrudService;

@RestController
@RequestMapping("/api/v1/menu-variant")
public class MenuVariantController extends BaseCrudController {
    public MenuVariantController(CrudService service) { super("menu_variant", service); }
}

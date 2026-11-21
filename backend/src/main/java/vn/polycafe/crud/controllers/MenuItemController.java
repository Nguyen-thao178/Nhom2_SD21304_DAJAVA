package vn.polycafe.crud.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.polycafe.crud.BaseCrudController;
import vn.polycafe.crud.CrudService;

@RestController
@RequestMapping("/api/v1/menu-item")
public class MenuItemController extends BaseCrudController {
    public MenuItemController(CrudService service) { super("menu_item", service); }
}

package vn.polycafe.crud.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.polycafe.crud.BaseCrudController;
import vn.polycafe.crud.CrudService;

@RestController
@RequestMapping("/api/v1/ingredient")
public class IngredientController extends BaseCrudController {
    public IngredientController(CrudService service) { super("ingredient", service); }
}

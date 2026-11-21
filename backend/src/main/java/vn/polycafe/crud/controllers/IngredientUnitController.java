package vn.polycafe.crud.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.polycafe.crud.BaseCrudController;
import vn.polycafe.crud.CrudService;

@RestController
@RequestMapping("/api/v1/ingredient-unit")
public class IngredientUnitController extends BaseCrudController {
    public IngredientUnitController(CrudService service) { super("ingredient_unit", service); }
}

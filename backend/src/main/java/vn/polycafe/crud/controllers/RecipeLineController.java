package vn.polycafe.crud.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.polycafe.crud.BaseCrudController;
import vn.polycafe.crud.CrudService;

@RestController
@RequestMapping("/api/v1/recipe-line")
public class RecipeLineController extends BaseCrudController {
    public RecipeLineController(CrudService service) { super("recipe_line", service); }
}

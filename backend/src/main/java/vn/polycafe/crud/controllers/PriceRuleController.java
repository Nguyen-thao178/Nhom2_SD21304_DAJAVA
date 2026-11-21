package vn.polycafe.crud.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.polycafe.crud.BaseCrudController;
import vn.polycafe.crud.CrudService;

@RestController
@RequestMapping("/api/v1/price-rule")
public class PriceRuleController extends BaseCrudController {
    public PriceRuleController(CrudService service) { super("price_rule", service); }
}

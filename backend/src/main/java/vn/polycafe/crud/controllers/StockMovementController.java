package vn.polycafe.crud.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.polycafe.crud.BaseCrudController;
import vn.polycafe.crud.CrudService;

@RestController
@RequestMapping("/api/v1/stock-movement")
public class StockMovementController extends BaseCrudController {
    public StockMovementController(CrudService service) { super("stock_movement", service); }
}

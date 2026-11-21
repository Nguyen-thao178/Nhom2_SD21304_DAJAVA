package vn.polycafe.crud.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.polycafe.crud.BaseCrudController;
import vn.polycafe.crud.CrudService;

@RestController
@RequestMapping("/api/v1/stock-lot")
public class StockLotController extends BaseCrudController {
    public StockLotController(CrudService service) { super("stock_lot", service); }
}

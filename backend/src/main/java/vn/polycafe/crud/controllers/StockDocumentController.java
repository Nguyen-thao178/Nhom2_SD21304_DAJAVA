package vn.polycafe.crud.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.polycafe.crud.BaseCrudController;
import vn.polycafe.crud.CrudService;

@RestController
@RequestMapping("/api/v1/stock-document")
public class StockDocumentController extends BaseCrudController {
    public StockDocumentController(CrudService service) { super("stock_document", service); }
}

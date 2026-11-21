package vn.polycafe.crud.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.polycafe.crud.BaseCrudController;
import vn.polycafe.crud.CrudService;

@RestController
@RequestMapping("/api/v1/stock-document-line")
public class StockDocumentLineController extends BaseCrudController {
    public StockDocumentLineController(CrudService service) { super("stock_document_line", service); }
}

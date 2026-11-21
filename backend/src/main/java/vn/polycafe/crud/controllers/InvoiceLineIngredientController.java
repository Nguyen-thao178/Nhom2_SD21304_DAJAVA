package vn.polycafe.crud.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.polycafe.crud.BaseCrudController;
import vn.polycafe.crud.CrudService;

@RestController
@RequestMapping("/api/v1/invoice-line-ingredient")
public class InvoiceLineIngredientController extends BaseCrudController {
    public InvoiceLineIngredientController(CrudService service) { super("invoice_line_ingredient", service); }
}

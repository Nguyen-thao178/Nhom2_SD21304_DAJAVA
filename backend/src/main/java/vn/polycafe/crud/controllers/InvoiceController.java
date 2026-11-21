package vn.polycafe.crud.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.polycafe.crud.BaseCrudController;
import vn.polycafe.crud.CrudService;

@RestController
@RequestMapping("/api/v1/invoice")
public class InvoiceController extends BaseCrudController {
    public InvoiceController(CrudService service) { super("invoice", service); }
}

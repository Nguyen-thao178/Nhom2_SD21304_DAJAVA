package vn.polycafe.crud.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.polycafe.crud.BaseCrudController;
import vn.polycafe.crud.CrudService;

@RestController
@RequestMapping("/api/v1/purchase-order")
public class PurchaseOrderController extends BaseCrudController {
    public PurchaseOrderController(CrudService service) { super("purchase_order", service); }
}

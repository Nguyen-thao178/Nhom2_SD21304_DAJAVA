package vn.polycafe.crud.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.polycafe.crud.BaseCrudController;
import vn.polycafe.crud.CrudService;

@RestController
@RequestMapping("/api/v1/purchase-order-line")
public class PurchaseOrderLineController extends BaseCrudController {
    public PurchaseOrderLineController(CrudService service) { super("purchase_order_line", service); }
}

package vn.polycafe.crud.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.polycafe.crud.BaseCrudController;
import vn.polycafe.crud.CrudService;

@RestController
@RequestMapping("/api/v1/point-ledger")
public class PointLedgerController extends BaseCrudController {
    public PointLedgerController(CrudService service) { super("point_ledger", service); }
}

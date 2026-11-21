package vn.polycafe.crud.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.polycafe.crud.BaseCrudController;
import vn.polycafe.crud.CrudService;

@RestController
@RequestMapping("/api/v1/pager-device")
public class PagerDeviceController extends BaseCrudController {
    public PagerDeviceController(CrudService service) { super("pager_device", service); }
}

package vn.polycafe.crud.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.polycafe.crud.BaseCrudController;
import vn.polycafe.crud.CrudService;

@RestController
@RequestMapping("/api/v1/async-operation")
public class AsyncOperationController extends BaseCrudController {
    public AsyncOperationController(CrudService service) { super("async_operation", service); }
}

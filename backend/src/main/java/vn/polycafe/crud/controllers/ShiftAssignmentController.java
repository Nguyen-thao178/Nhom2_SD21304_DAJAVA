package vn.polycafe.crud.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.polycafe.crud.BaseCrudController;
import vn.polycafe.crud.CrudService;

@RestController
@RequestMapping("/api/v1/shift-assignment")
public class ShiftAssignmentController extends BaseCrudController {
    public ShiftAssignmentController(CrudService service) { super("shift_assignment", service); }
}

package vn.polycafe.crud;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import vn.polycafe.auth.StaffUser;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/database")
public class DatabaseStatusController {
    private final JdbcTemplate db;
    private final CrudService service;
    public DatabaseStatusController(JdbcTemplate db,CrudService service) {this.db=db;this.service=service;}
    @GetMapping("/status")
    public Map<String,Object> status(@AuthenticationPrincipal StaffUser user) {
        service.authorize(user);
        Integer available=db.queryForObject("SELECT count(*) FROM information_schema.tables WHERE table_schema='public' AND table_type='BASE TABLE'",Integer.class);
        return Map.of("connected",db.queryForObject("SELECT 1",Integer.class)==1,"tables",available,"mappedTables",service.metadata(user).size());
    }
    @GetMapping("/catalog")
    public Object catalog(@AuthenticationPrincipal StaffUser user) {return service.metadata(user);}
}

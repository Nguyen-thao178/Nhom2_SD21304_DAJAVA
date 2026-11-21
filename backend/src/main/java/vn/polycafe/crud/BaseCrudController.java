package vn.polycafe.crud;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import vn.polycafe.auth.StaffUser;
import java.time.OffsetDateTime;
import java.util.*;

/** Inherited by one explicitly mapped controller per database table. */
public abstract class BaseCrudController {
    private final String table;
    private final CrudService service;
    protected BaseCrudController(String table, CrudService service) { this.table=table; this.service=service; }
    @GetMapping
    public List<Map<String,Object>> list(@RequestParam(required=false) UUID after,
            @RequestParam(defaultValue="50") int size, @AuthenticationPrincipal StaffUser user) {
        return service.list(table,after,size,user);
    }
    @GetMapping("/schema")
    public Object schema(@AuthenticationPrincipal StaffUser user) { return service.metadata(table,user); }
    @GetMapping("/{id}")
    public Map<String,Object> get(@PathVariable UUID id,@AuthenticationPrincipal StaffUser user) { return service.get(table,id,user); }
    @PostMapping(consumes="application/json") @ResponseStatus(HttpStatus.CREATED)
    public Map<String,Object> create(@RequestBody Map<String,Object> body,@AuthenticationPrincipal StaffUser user) { return service.create(table,body,user); }
    @PatchMapping(value="/{id}",consumes="application/json")
    public Map<String,Object> update(@PathVariable UUID id,@RequestBody Map<String,Object> body,
            @RequestHeader("X-Expected-Updated-At") OffsetDateTime expected,@AuthenticationPrincipal StaffUser user) {
        return service.update(table,id,body,expected,user);
    }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id,@RequestHeader("X-Expected-Updated-At") OffsetDateTime expected,
                       @AuthenticationPrincipal StaffUser user) { service.delete(table,id,expected,user); }
}

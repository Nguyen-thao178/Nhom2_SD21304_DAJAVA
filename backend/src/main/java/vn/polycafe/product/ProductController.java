package vn.polycafe.product;

import java.util.*;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import vn.polycafe.auth.StaffUser;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService service;
    public ProductController(ProductService service) {this.service=service;}
    @GetMapping public List<Product> list(@RequestParam(defaultValue="") String q,@RequestParam(defaultValue="") String status,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="12") int size) {return service.list(q,status,page,size);}
    @GetMapping("/{id}") public Product get(@PathVariable UUID id) {return service.get(id);}
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public Product create(@Valid @RequestBody ProductInput body,@AuthenticationPrincipal StaffUser user) {return service.create(body,user);}
    @PutMapping("/{id}") public Product update(@PathVariable UUID id,@Valid @RequestBody ProductInput body,@AuthenticationPrincipal StaffUser user) {return service.update(id,body,user);}
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void archive(@PathVariable UUID id,@AuthenticationPrincipal StaffUser user) {service.archive(id,user);}
}

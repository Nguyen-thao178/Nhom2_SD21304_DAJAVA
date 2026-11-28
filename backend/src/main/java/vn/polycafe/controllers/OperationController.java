package vn.polycafe.controllers;

import jakarta.validation.Valid;
import java.util.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import vn.polycafe.api.WorkflowRequests.*;
import vn.polycafe.api.WorkflowRequests.Collection;
import vn.polycafe.auth.StaffUser;
import vn.polycafe.services.*;

@RestController @RequestMapping("/api/v1/operations")
public class OperationController {
    private final OperationQueryService queries;private final OrderService orders;private final PaymentService payments;private final TicketService tickets;private final ShiftService shifts;
    public OperationController(OperationQueryService queries,OrderService orders,PaymentService payments,TicketService tickets,ShiftService shifts){this.queries=queries;this.orders=orders;this.payments=payments;this.tickets=tickets;this.shifts=shifts;}
    @GetMapping("/branches") Object branches(@AuthenticationPrincipal StaffUser u){return queries.branches(u);}
    @GetMapping("/capabilities") Object capabilities(@RequestParam UUID branch,@AuthenticationPrincipal StaffUser u){return queries.capabilities(branch,u);}
    @PostMapping("/quote") Object quote(@Valid @RequestBody NewOrder r,@AuthenticationPrincipal StaffUser u){return orders.quote(r,u);}
    @GetMapping("/references/{kind}") Object references(@PathVariable String kind,@RequestParam UUID branch,@RequestParam(defaultValue="") String q,@AuthenticationPrincipal StaffUser u){return queries.references(branch,kind,q,u);}
    @GetMapping("/menu") Object menu(@RequestParam UUID branch,@RequestParam(defaultValue="counter") String channel,@AuthenticationPrincipal StaffUser u){return queries.menu(branch,channel,u);}
    @GetMapping("/orders") Object list(@RequestParam UUID branch,@AuthenticationPrincipal StaffUser u){return queries.list(branch,u);}
    @GetMapping("/orders/{id}") Object detail(@PathVariable UUID id,@AuthenticationPrincipal StaffUser u){return queries.detail(id,u);}
    @PostMapping("/orders") Object create(@Valid @RequestBody NewOrder r,@AuthenticationPrincipal StaffUser u){return orders.create(r,u);}
    @PostMapping("/orders/{id}/confirm") Object confirm(@PathVariable UUID id,@Valid @RequestBody Confirmation r,@AuthenticationPrincipal StaffUser u){return orders.confirm(id,r,u);}
    @PostMapping("/orders/{id}/reject") Object reject(@PathVariable UUID id,@Valid @RequestBody Command r,@AuthenticationPrincipal StaffUser u){return orders.reject(id,r,u);}
    @PostMapping("/lines/{id}/{action:accept|start|ready|cancel}") Object line(@PathVariable UUID id,@PathVariable String action,@Valid @RequestBody Command r,@AuthenticationPrincipal StaffUser u){return orders.line(id,action,r,u);}
    @PostMapping("/orders/{id}/payments") Object collect(@PathVariable UUID id,@Valid @RequestBody Collection r,@AuthenticationPrincipal StaffUser u){return payments.collect(id,r,u);}
    @PostMapping("/orders/{id}/refund-requests") Object refundRequest(@PathVariable UUID id,@Valid @RequestBody RefundRequest r,@AuthenticationPrincipal StaffUser u){return payments.requestRefund(id,r,u);}
    @PostMapping("/approvals/{id}/decision") Object decide(@PathVariable UUID id,@Valid @RequestBody Decision r,@AuthenticationPrincipal StaffUser u){return payments.decide(id,r,u);}
    @PostMapping("/orders/{id}/refunds") Object refund(@PathVariable UUID id,@Valid @RequestBody Refund r,@AuthenticationPrincipal StaffUser u){return payments.refund(id,r,u);}
    @PostMapping("/tickets/{id}/pager") Object assign(@PathVariable UUID id,@Valid @RequestBody AssignPager r,@AuthenticationPrincipal StaffUser u){return tickets.assign(id,r,u);}
    @PostMapping("/tickets/{id}/handover") Object handover(@PathVariable UUID id,@Valid @RequestBody Handover r,@AuthenticationPrincipal StaffUser u){return tickets.handover(id,r,u);}
    @PostMapping("/tickets/{id}/{action:call|return|close|reopen}") Object ticket(@PathVariable UUID id,@PathVariable String action,@Valid @RequestBody Command r,@AuthenticationPrincipal StaffUser u){return tickets.command(id,action,r,u);}
    @GetMapping("/shifts") Object shiftList(@RequestParam UUID branch,@AuthenticationPrincipal StaffUser u){return queries.shiftList(branch,u);}
    @PostMapping("/shifts") Object open(@Valid @RequestBody OpenShift r,@AuthenticationPrincipal StaffUser u){return shifts.open(r,u);}
    @PostMapping("/shifts/{id}/close") Object close(@PathVariable UUID id,@Valid @RequestBody CloseShift r,@AuthenticationPrincipal StaffUser u){return shifts.close(id,r,u);}
    @GetMapping("/audit") Object audit(@RequestParam UUID branch,@RequestParam(required=false) UUID invoice,@AuthenticationPrincipal StaffUser u){return queries.audit(branch,invoice,u);}
}

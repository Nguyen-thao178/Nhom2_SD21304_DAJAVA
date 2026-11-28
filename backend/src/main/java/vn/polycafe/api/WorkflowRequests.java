package vn.polycafe.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.*;

/** HTTP inputs contain choices, never trusted totals, actors or lifecycle states. */
public final class WorkflowRequests {
    private WorkflowRequests() {}
    public record Line(@NotNull UUID itemId,@Min(1) @Max(99) int quantity,UUID sizeId,
                       @Size(max=10) List<UUID> optionIds,@Size(max=300) String note) {}
    public record NewOrder(@NotNull UUID requestId,@NotNull UUID branchId,UUID tableId,UUID customerId,
                           @NotBlank @Pattern(regexp="counter|table|takeaway") String channel,
                           @NotEmpty @Size(max=50) List<@Valid Line> lines) {}
    public record Command(@NotNull UUID requestId,@Size(max=300) String reason) {}
    public record Confirmation(@NotNull UUID requestId,@NotNull @DecimalMin("0") BigDecimal expectedTotal) {}
    public record Collection(@NotNull UUID requestId,@NotBlank @Pattern(regexp="cash|transfer|card|wallet") String method,
                             @NotNull @DecimalMin("1") @Digits(integer=14,fraction=0) BigDecimal amount,
                             @NotBlank @Size(max=80) String txRef,@Size(max=300) String note) {}
    public record RefundRequest(@NotNull UUID requestId,@NotNull UUID paymentId,
                                @NotNull @DecimalMin("1") @Digits(integer=14,fraction=0) BigDecimal amount,
                                @NotBlank @Size(max=300) String reason) {}
    public record Decision(@NotNull UUID requestId,boolean approve,@NotBlank @Size(max=300) String reason) {}
    public record Refund(@NotNull UUID requestId,@NotNull UUID approvalId,@NotBlank @Size(max=80) String txRef,
                         @NotBlank @Size(max=300) String note) {}
    public record Handover(@NotNull UUID requestId,@NotNull UUID lineId,@Min(1) @Max(99) int quantity,
                           @NotBlank @Size(max=100) String receiver,@Size(max=300) String note) {}
    public record AssignPager(@NotNull UUID requestId,@NotNull UUID pagerId) {}
    public record OpenShift(@NotNull UUID requestId,@NotNull UUID branchId,
                            @NotNull @DecimalMin("0") @Digits(integer=14,fraction=0) BigDecimal openingCash) {}
    public record CloseShift(@NotNull UUID requestId,@NotNull @DecimalMin("0") @Digits(integer=14,fraction=0) BigDecimal countedCash,
                             @Size(max=300) String reason) {}
    public record StaffAccount(@NotNull UUID branchId,@NotBlank @Pattern(regexp="[A-Za-z0-9_.-]{3,60}") String username,
                               @NotBlank @Size(max=150) String fullName,@NotBlank @Size(min=12,max=72) String password) {
        @Override public String toString(){return "StaffAccount[credentials=redacted]";}
    }
}

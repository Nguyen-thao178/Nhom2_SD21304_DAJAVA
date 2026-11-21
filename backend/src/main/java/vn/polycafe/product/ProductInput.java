package vn.polycafe.product;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import jakarta.validation.constraints.*;

public record ProductInput(
    @NotBlank @Size(max=150) String name,
    @Size(max=500) String description,
    @NotBlank @Pattern(regexp="[A-Z0-9_]{1,40}",message="Mã danh mục chỉ gồm A-Z, 0-9 và dấu _.") String categoryCode,
    @NotBlank @Size(max=100) String categoryName,
    @NotBlank @Pattern(regexp="drink|food") String kind,
    @NotBlank @Pattern(regexp="bar|kitchen|pack") String stationKind,
    @NotBlank @Pattern(regexp="active|paused|seasonal|sold_out|archived") String status,
    @Size(max=200) String allergenNote,
    @NotNull @DecimalMin("0") @Digits(integer=14,fraction=0) BigDecimal price,
    OffsetDateTime expectedUpdatedAt
) {}

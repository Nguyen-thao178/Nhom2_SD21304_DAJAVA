package vn.polycafe.catalog;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record Product(UUID id, String name, String description, String categoryCode, String categoryName, String kind,
                String stationKind, String status, String allergenNote, BigDecimal price, OffsetDateTime updatedAt) {
}

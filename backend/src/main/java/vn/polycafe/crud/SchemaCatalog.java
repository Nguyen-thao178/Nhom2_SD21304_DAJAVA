package vn.polycafe.crud;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import vn.polycafe.common.ApiException;
import java.io.IOException;
import java.util.*;

/** Immutable allowlist captured from the actual PolyCafe database, never from a request. */
@Component
public class SchemaCatalog {
    public record Column(String name, String type, boolean nullable, boolean hasDefault,
                         Integer maxLength, Integer precision, Integer scale) {}
    public record Table(String name, List<Column> columns, List<String> checks) {
        public Column column(String field) {
            return columns.stream().filter(c -> c.name().equals(field)).findFirst()
                .orElseThrow(() -> new ApiException(400, "Thuộc tính không hợp lệ: " + field));
        }
        public boolean has(String field) { return columns.stream().anyMatch(c -> c.name().equals(field)); }
    }
    private final Map<String, Table> tables;
    public SchemaCatalog(ObjectMapper json) throws IOException {
        try (var input = new ClassPathResource("crud-catalog.json").getInputStream()) {
            tables = Collections.unmodifiableMap(json.readValue(input, new TypeReference<Map<String, Table>>() {}));
        }
    }
    public Table table(String name) {
        var table = tables.get(name);
        if (table == null) throw new ApiException(404, "Không có tài nguyên này.");
        return table;
    }
    public Collection<Table> tables() { return tables.values(); }
}

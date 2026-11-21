package vn.polycafe.crud;

import com.fasterxml.jackson.databind.*;
import org.springframework.stereotype.Component;
import vn.polycafe.common.ApiException;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Component
public class PayloadValidator {
    private final ObjectMapper json;
    public PayloadValidator(ObjectMapper json) { this.json = json; }
    public Map<String,Object> validate(SchemaCatalog.Table table, Map<String,Object> input) {
        if (input == null || input.isEmpty() || input.size() > 80) throw new ApiException(400, "Body phải có 1–80 thuộc tính.");
        Map<String,Object> result = new LinkedHashMap<>();
        for (var entry : input.entrySet()) {
            var c = table.column(entry.getKey());
            Object value = entry.getValue();
            if (value == null) {
                if (!c.nullable()) throw invalid(c.name());
                result.put(c.name(), null); continue;
            }
            try { result.put(c.name(), convert(c, value)); }
            catch (ApiException e) { throw e; }
            catch (Exception e) { throw invalid(c.name()); }
        }
        return result;
    }
    private Object convert(SchemaCatalog.Column c, Object v) throws Exception {
        return switch (c.type()) {
            case "uuid" -> UUID.fromString(text(v));
            case "int4" -> new BigDecimal(textOrNumber(v)).intValueExact();
            case "int8" -> new BigDecimal(textOrNumber(v)).longValueExact();
            case "numeric" -> {
                BigDecimal number = new BigDecimal(textOrNumber(v));
                if (c.scale() != null) number = number.setScale(c.scale(), java.math.RoundingMode.UNNECESSARY);
                if (c.precision() != null && number.precision() > c.precision()) throw invalid(c.name());
                yield number;
            }
            case "bool" -> { if (!(v instanceof Boolean)) throw invalid(c.name()); yield v; }
            case "date" -> LocalDate.parse(text(v));
            case "time" -> LocalTime.parse(text(v));
            case "timestamptz" -> OffsetDateTime.parse(text(v));
            case "jsonb" -> {
                JsonNode node = json.valueToTree(v);
                if (!(node.isObject() || node.isArray())) throw invalid(c.name());
                String encoded = json.writeValueAsString(node);
                if (encoded.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 262144) throw invalid(c.name());
                yield encoded;
            }
            case "_uuid" -> {
                if (!(v instanceof List<?> list) || list.size() > 1000) throw invalid(c.name());
                yield "{" + String.join(",", list.stream().map(x -> UUID.fromString(text(x)).toString()).toList()) + "}";
            }
            default -> {
                String str = text(v);
                if (str.indexOf('\0') >= 0 || str.length() > (c.maxLength() == null ? 10000 : c.maxLength())) throw invalid(c.name());
                yield str;
            }
        };
    }
    private String text(Object value) {
        if (!(value instanceof String s)) throw new IllegalArgumentException();
        return s;
    }
    private String textOrNumber(Object value) {
        if (!(value instanceof String || value instanceof Number)) throw new IllegalArgumentException();
        return value.toString();
    }
    private ApiException invalid(String field) { return new ApiException(400, "Sai kiểu, độ dài hoặc độ chính xác của " + field); }
}

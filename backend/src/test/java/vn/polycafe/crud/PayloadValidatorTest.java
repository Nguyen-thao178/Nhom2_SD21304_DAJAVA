package vn.polycafe.crud;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import vn.polycafe.common.ApiException;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class PayloadValidatorTest {
    private SchemaCatalog catalog;
    private PayloadValidator validator;
    @BeforeEach void setup() throws Exception {var json=new ObjectMapper();catalog=new SchemaCatalog(json);validator=new PayloadValidator(json);}
    @Test void rejectsFractionalIntegerAndOutOfRangeMoney() {
        assertThrows(ApiException.class,()->validator.validate(catalog.table("invoice_line"),Map.of("quantity",1.5)));
        assertThrows(ApiException.class,()->validator.validate(catalog.table("payment"),Map.of("amount","99999999999999999999999")));
        assertThrows(ApiException.class,()->validator.validate(catalog.table("payment"),Map.of("amount","1.01")));
    }
    @Test void sqlPayloadRemainsData() {
        String payload="x'; DROP TABLE supplier; --";
        assertEquals(payload,validator.validate(catalog.table("supplier"),Map.of("name",payload)).get("name"));
    }
    @Test void rejectsInvalidUuidAndOversizedField() {
        assertThrows(ApiException.class,()->validator.validate(catalog.table("invoice"),Map.of("branch_id","not-uuid")));
        assertThrows(ApiException.class,()->validator.validate(catalog.table("branch"),Map.of("code","x".repeat(21))));
    }
}

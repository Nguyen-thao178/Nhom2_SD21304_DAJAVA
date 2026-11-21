package vn.polycafe.crud;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import vn.polycafe.auth.StaffUser;
import vn.polycafe.common.*;
import java.util.*;
import java.time.OffsetDateTime;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CrudSecurityTest {
    private SchemaCatalog catalog;
    private CrudRepository repository;
    private JdbcTemplate db;
    private CrudService service;
    private final StaffUser owner=new StaffUser(UUID.randomUUID(),"owner","Owner",0,Set.of("owner"),true);
    @BeforeEach void setup() throws Exception {
        ObjectMapper json=new ObjectMapper();catalog=new SchemaCatalog(json);
        repository=mock(CrudRepository.class); db=mock(JdbcTemplate.class);
        service=new CrudService(catalog,repository,new PayloadValidator(json),mock(AuditService.class),mock(PasswordEncoder.class),db);
    }
    @Test void rejectsDynamicSqlIdentifiers() {
        assertThrows(ApiException.class,()->catalog.table("invoice; DROP TABLE app_user;--"));
        assertThrows(ApiException.class,()->new PayloadValidator(new ObjectMapper()).validate(catalog.table("ingredient"),Map.of("name) VALUES ('x'); --","x")));
        verifyNoInteractions(repository,db);
    }
    @Test void allActualTablesHaveRegisteredRoutes() throws Exception {
        assertEquals(39,catalog.tables().size());
        for(var table:catalog.tables()) {
            String type=Arrays.stream(table.name().split("_")).map(x->Character.toUpperCase(x.charAt(0))+x.substring(1)).reduce("",String::concat);
            assertTrue(BaseCrudController.class.isAssignableFrom(Class.forName("vn.polycafe.crud.controllers."+type+"Controller")));
        }
    }
    @Test void staffCannotUseGlobalAdministration() {
        var staff=new StaffUser(UUID.randomUUID(),"staff","Staff",0,Set.of("staff"),false);
        assertEquals(403,assertThrows(ApiException.class,()->service.list("invoice",null,50,staff)).status);
        verifyNoInteractions(repository,db);
    }
    @Test void journalAndSnapshotCannotBeModified() {
        for(String table:List.of("audit_log","stock_movement","point_ledger","fulfillment_event","invoice_line_ingredient")) {
            assertEquals(405,assertThrows(ApiException.class,()->service.update(table,UUID.randomUUID(),Map.of("kind","adjust"),OffsetDateTime.now(),owner)).status);
            assertEquals(405,assertThrows(ApiException.class,()->service.delete(table,UUID.randomUUID(),OffsetDateTime.now(),owner)).status);
        }
        verifyNoInteractions(repository,db);
    }
    @Test void readNeverReturnsHashesOrIntegrationSecrets() {
        UUID id=UUID.randomUUID();
        when(repository.get(catalog.table("app_user"),id,false)).thenReturn(Map.of("id",id,"password_hash","secret","pin_hash","secret"));
        assertFalse(service.get("app_user",id,owner).containsKey("password_hash"));
        assertFalse(service.get("app_user",id,owner).containsKey("pin_hash"));
    }
    @Test void cannotInjectDerivedBalancesOnCreate() {
        var failure=assertThrows(ApiException.class,()->service.create("customer",Map.of("member_code","A123","kind","member","status","active","points_balance",100000),owner));
        assertEquals(400,failure.status);
        verifyNoInteractions(repository,db);
    }
    @Test void staleUpdateCannotOverwriteAnotherWriter() {
        UUID id=UUID.randomUUID();OffsetDateTime now=OffsetDateTime.now();
        when(repository.get(catalog.table("ingredient"),id,true)).thenReturn(Map.of("id",id,"updated_at",now));
        assertEquals(409,assertThrows(ApiException.class,()->service.update("ingredient",id,Map.of("name","changed"),now.minusSeconds(1),owner)).status);
        verify(repository,never()).update(any(),any(),any());
    }
    @Test void confirmedInvoiceIsNotEditableThroughCrud() {
        UUID id=UUID.randomUUID();OffsetDateTime now=OffsetDateTime.now();
        when(repository.get(catalog.table("invoice"),id,true)).thenReturn(Map.of("id",id,"updated_at",now,"status","confirmed"));
        assertEquals(409,assertThrows(ApiException.class,()->service.update("invoice",id,Map.of("total",0),now,owner)).status);
        verify(repository,never()).update(any(),any(),any());
    }
    @Test void stockUnderflowFailsBeforeWritingLedgerOrCache() {
        UUID lot=UUID.randomUUID(),branch=UUID.randomUUID(),ingredient=UUID.randomUUID();
        when(repository.get(catalog.table("stock_lot"),lot,true)).thenReturn(Map.of("qty_remaining",10,"reserved_qty",5,"branch_id",branch,"ingredient_id",ingredient));
        var body=new LinkedHashMap<String,Object>();
        body.put("lot_id",lot.toString());body.put("branch_id",branch.toString());body.put("ingredient_id",ingredient.toString());
        body.put("kind","consume");body.put("qty_base",-20);body.put("ref_type","invoice_line");body.put("ref_id",UUID.randomUUID().toString());body.put("operation_key","test");body.put("seq_no",0);
        assertEquals(409,assertThrows(ApiException.class,()->service.create("stock_movement",body,owner)).status);
        verify(repository,never()).insert(any(),any());verify(db,never()).update(anyString(),any(Object[].class));
    }
}

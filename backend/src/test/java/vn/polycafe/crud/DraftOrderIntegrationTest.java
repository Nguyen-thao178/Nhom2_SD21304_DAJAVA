package vn.polycafe.crud;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.junit.jupiter.api.Assertions.*;

class DraftOrderIntegrationTest extends HttpCrudIntegrationTest {
    @Autowired SchemaCatalog catalog;
    UUID branch,item;
    @BeforeEach void orderFixture() {
        for(String table:List.of("branch","menu_item","price_rule","customer","invoice","invoice_line")) {
            var columns=new ArrayList<String>();
            for(var column:catalog.table(table).columns()) {
                String type=switch(column.type()) {
                    case "timestamptz" -> "timestamp with time zone";
                    case "int4" -> "integer";
                    default -> column.type();
                };
                String definition=column.name()+" "+type;
                if(column.type().equals("varchar")) definition+="("+column.maxLength()+")";
                if(column.name().equals("id")) definition+=" default random_uuid() primary key";
                else if(column.type().equals("timestamptz") && column.hasDefault()) definition+=" default current_timestamp";
                else if(column.hasDefault() && Set.of("integer","numeric").contains(type)) definition+=" default 0";
                else if(column.type().equals("bool")) definition+=" default false";
                columns.add(definition);
            }
            db.execute("CREATE TABLE IF NOT EXISTS "+table+"("+String.join(",",columns)+")");
            db.update("DELETE FROM "+table);
        }
        branch=UUID.randomUUID();item=UUID.randomUUID();
        db.update("INSERT INTO branch(id,timezone,business_day_cutoff,status) VALUES(?,'Asia/Ho_Chi_Minh','04:00:00','active')",branch);
        db.update("INSERT INTO menu_item(id,name,kind,status,station_kind) VALUES(?,'Coffee','drink','active','bar')",item);
        db.update("INSERT INTO price_rule(id,item_id,price,valid_from) VALUES(?,?,25000,?)",UUID.randomUUID(),item,java.time.OffsetDateTime.now().minusDays(1));
    }
    String body(UUID key,int qty) throws Exception {
        return json.writeValueAsString(Map.of("branchId",branch,"requestId",key,"lines",List.of(Map.of("itemId",item,"quantity",qty)),"note","Ít ngọt"));
    }
    @Test void savesWholeDraftAtServerPriceAndReplaysWithoutDuplication() throws Exception {
        UUID key=UUID.randomUUID();
        String result=http.perform(post("/api/ui/draft-orders").session(session).with(csrf()).contentType("application/json").content(body(key,2)))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("draft")).andExpect(jsonPath("$.total").value(50000))
            .andReturn().getResponse().getContentAsString();
        String id=json.readTree(result).get("id").asText();
        http.perform(post("/api/ui/draft-orders").session(session).with(csrf()).contentType("application/json").content(body(key,2)))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(id));
        assertEquals(1,db.queryForObject("SELECT count(*) FROM invoice",Integer.class));
        assertEquals(1,db.queryForObject("SELECT count(*) FROM invoice_line",Integer.class));
        assertEquals(1,db.queryForObject("SELECT count(*) FROM customer",Integer.class));
        http.perform(post("/api/ui/draft-orders").session(session).with(csrf()).contentType("application/json").content(body(key,3)))
            .andExpect(status().isConflict());
    }
    @Test void rejectedItemDoesNotLeavePartialDraftOrGuest() throws Exception {
        db.update("UPDATE menu_item SET status='sold_out' WHERE id=?",item);
        http.perform(post("/api/ui/draft-orders").session(session).with(csrf()).contentType("application/json").content(body(UUID.randomUUID(),1)))
            .andExpect(status().isConflict());
        assertEquals(0,db.queryForObject("SELECT count(*) FROM invoice",Integer.class));
        assertEquals(0,db.queryForObject("SELECT count(*) FROM customer",Integer.class));
    }
}

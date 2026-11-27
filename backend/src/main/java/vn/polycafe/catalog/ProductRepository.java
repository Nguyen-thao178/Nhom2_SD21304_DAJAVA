package vn.polycafe.catalog;

import java.sql.*;
import java.util.*;
import java.time.OffsetDateTime;
import java.math.BigDecimal;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ProductRepository {
    private final JdbcTemplate db;
    public ProductRepository(JdbcTemplate db) { this.db=db; }
    private static final String SELECT="""
        SELECT m.*, (SELECT p.price FROM price_rule p WHERE p.item_id=m.id AND p.branch_id IS NULL
        AND p.channel IS NULL AND p.valid_from<=CURRENT_TIMESTAMP AND (p.valid_to IS NULL OR p.valid_to>CURRENT_TIMESTAMP)
        ORDER BY p.valid_from DESC LIMIT 1) AS price FROM menu_item m
        """;
    private Product row(ResultSet r,int n) throws SQLException {
        return new Product(r.getObject("id",UUID.class),r.getString("name"),r.getString("description"),r.getString("category_code"),r.getString("category_name"),r.getString("kind"),r.getString("station_kind"),r.getString("status"),r.getString("allergen_note"),r.getBigDecimal("price"),r.getObject("updated_at",OffsetDateTime.class));
    }
    public List<Product> list(String search,String status,int page,int size) {
        String query=SELECT+" WHERE m.kind IN ('drink','food') AND (LOWER(m.name) LIKE ? ESCAPE '!' OR LOWER(m.category_name) LIKE ? ESCAPE '!')";
        var args=new ArrayList<Object>(); String pattern="%"+search.toLowerCase(Locale.ROOT).replace("!","!!").replace("%","!%").replace("_","!_")+"%";
        args.add(pattern);args.add(pattern);
        if(!status.isEmpty()) {query+=" AND m.status=?";args.add(status);}
        query+=" ORDER BY m.created_at DESC,m.id LIMIT ? OFFSET ?";args.add(size);args.add(page*size);
        return db.query(query,this::row,args.toArray());
    }
    public Optional<Product> get(UUID id,boolean lock) {
        if(lock) db.queryForList("SELECT id FROM menu_item WHERE id=? FOR UPDATE",id);
        return db.query(SELECT+" WHERE m.id=? AND m.kind IN ('drink','food')",this::row,id).stream().findFirst();
    }
    public UUID insert(ProductInput p) {
        UUID id=UUID.randomUUID();
        db.update("INSERT INTO menu_item(id,name,description,category_code,category_name,kind,station_kind,status,allergen_note) VALUES(?,?,?,?,?,?,?,?,?)",id,p.name().trim(),p.description(),p.categoryCode(),p.categoryName().trim(),p.kind(),p.stationKind(),p.status(),p.allergenNote()); return id;
    }
    public void update(UUID id,ProductInput p) {
        db.update("UPDATE menu_item SET name=?,description=?,category_code=?,category_name=?,kind=?,station_kind=?,status=?,allergen_note=?,updated_at=CURRENT_TIMESTAMP WHERE id=?",p.name().trim(),p.description(),p.categoryCode(),p.categoryName().trim(),p.kind(),p.stationKind(),p.status(),p.allergenNote(),id);
    }
    public void archive(UUID id) { db.update("UPDATE menu_item SET status='archived',updated_at=CURRENT_TIMESTAMP WHERE id=?",id); }
    public void price(UUID id,BigDecimal amount,OffsetDateTime time) {
        db.update("UPDATE price_rule SET valid_to=?,updated_at=CURRENT_TIMESTAMP WHERE item_id=? AND branch_id IS NULL AND channel IS NULL AND valid_to IS NULL",time,id);
        db.update("INSERT INTO price_rule(item_id,price,valid_from) VALUES(?,?,?)",id,amount,time);
    }
    public OffsetDateTime databaseTime() { return db.queryForObject("SELECT CURRENT_TIMESTAMP",OffsetDateTime.class); }
    public boolean categoryConflict(String code,String name,UUID except) {
        return db.queryForObject("SELECT count(*) FROM menu_item WHERE category_code=? AND category_name<>? AND id<>?",Integer.class,code,name,except)>0;
    }
}

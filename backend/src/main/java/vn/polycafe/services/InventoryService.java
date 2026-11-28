package vn.polycafe.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import org.springframework.stereotype.Service;
import vn.polycafe.common.ApiException;
import vn.polycafe.dao.OperationRepository;

/** Reservations belong to a frozen line snapshot, not a mutable recipe. */
@Service
public class InventoryService {
    private final OperationRepository db;
    public InventoryService(OperationRepository db){this.db=db;}
    @SuppressWarnings("unchecked")
    public Map<String,Object> snapshot(Map<String,Object> order,Map<String,Object> line,PricingService.Quote quote,UUID actor){
        UUID branch=OperationRepository.id(order,"branch_id"),lineId=OperationRepository.id(line,"id"),item=OperationRepository.id(line,"item_id"),size=OperationRepository.id(line,"size_id");
        db.advisory("stock:"+branch);
        var recipes=db.rows("SELECT * FROM recipe WHERE item_id=? AND size_id IS NOT DISTINCT FROM CAST(? AS uuid) AND status='active' AND effective_from<=CURRENT_TIMESTAMP ORDER BY effective_from DESC,version DESC,id LIMIT 1",item,size);
        if(recipes.isEmpty())throw new ApiException(409,"Món chưa có công thức hiệu lực cho kích cỡ đã chọn.");
        var recipe=recipes.get(0);var quantities=new TreeMap<UUID,BigDecimal>();
        Set<UUID> chosen=new HashSet<>();for(var option:quote.options())chosen.add(OperationRepository.id(option,"id"));
        for(var row:db.rows("SELECT r.*,u.to_base_qty FROM recipe_line r JOIN ingredient_unit u ON u.id=r.unit_id AND u.ingredient_id=r.ingredient_id WHERE recipe_id=?",recipe.get("id"))){
            UUID option=OperationRepository.id(row,"option_id");if(option!=null&&!chosen.contains(option))continue;
            quantities.merge(OperationRepository.id(row,"ingredient_id"),OperationRepository.money(row.get("quantity")).multiply(OperationRepository.money(row.get("to_base_qty"))),BigDecimal::add);
        }
        for(var option:quote.options()){
            UUID ingredient=OperationRepository.id(option,"stock_ingredient_id");String action=Objects.toString(option.get("stock_action"),"none");
            if(action.equals("add")){var unit=db.one("SELECT to_base_qty FROM ingredient_unit WHERE id=? AND ingredient_id=?",option.get("stock_unit_id"),ingredient);quantities.merge(ingredient,OperationRepository.money(option.get("stock_quantity")).multiply(OperationRepository.money(unit.get("to_base_qty"))),BigDecimal::add);}
            else if(action.equals("scale"))quantities.computeIfPresent(ingredient,(k,v)->v.multiply(OperationRepository.money(option.get("stock_multiplier"))));
            else if(action.equals("replace")){
                UUID replacement=OperationRepository.id(option,"stock_replacement_ingredient_id");
                var units=db.rows("SELECT base_unit FROM ingredient WHERE id IN (?,?)",ingredient,replacement);
                if(units.size()!=2||!Objects.equals(units.get(0).get("base_unit"),units.get(1).get("base_unit")))throw new ApiException(409,"Nguyên liệu thay thế phải có cùng đơn vị cơ sở.");
                BigDecimal qty=quantities.remove(ingredient);if(qty!=null)quantities.merge(replacement,qty,BigDecimal::add);
            }
        }
        if(quantities.isEmpty())throw new ApiException(409,"Công thức chưa có định lượng.");
        var allocations=new ArrayList<Map<String,Object>>();int seq=0;
        for(var entry:quantities.entrySet()){
            BigDecimal needed=entry.getValue().multiply(OperationRepository.money(line.get("quantity")));if(needed.signum()<=0)continue;
            BigDecimal base=needed;
            var lots=db.rows("SELECT * FROM stock_lot WHERE branch_id=? AND ingredient_id=? AND (expiry_date IS NULL OR expiry_date>=CURRENT_DATE) AND qty_remaining>reserved_qty ORDER BY expiry_date NULLS LAST,id FOR UPDATE",branch,entry.getKey());
            for(var lot:lots){if(needed.signum()==0)break;BigDecimal take=needed.min(OperationRepository.money(lot.get("qty_remaining")).subtract(OperationRepository.money(lot.get("reserved_qty"))));
                UUID lotId=OperationRepository.id(lot,"id");db.write("UPDATE stock_lot SET reserved_qty=reserved_qty+? WHERE id=?",take,lotId);
                movement(branch,entry.getKey(),lotId,"reserve",take,lineId,actor,"confirm:"+lineId,seq++);
                allocations.add(Map.of("lot_id",lotId,"ingredient_id",entry.getKey(),"qty",take.toPlainString(),"unit_cost",OperationRepository.money(lot.get("unit_cost")).toPlainString()));needed=needed.subtract(take);
            }
            if(needed.signum()>0)throw new ApiException(409,"Không đủ nguyên liệu khả dụng; đơn chưa được xác nhận.");
            db.write("INSERT INTO invoice_line_ingredient(line_id,ingredient_id,qty_base,recipe_qty_base) VALUES(?,?,?,?)",lineId,entry.getKey(),base,base);
        }
        if(allocations.isEmpty())throw new ApiException(409,"Công thức phải có định lượng lớn hơn không.");
        db.write("UPDATE invoice_line SET recipe_id=? WHERE id=?",recipe.get("id"),lineId);
        return Map.of("schema_version",1,"inventory_allocations",allocations,"option_ids",chosen,"price",quote.price().toPlainString());
    }
    public void movement(UUID branch,UUID ingredient,UUID lot,String kind,BigDecimal qty,UUID line,UUID actor,String operation,int seq){
        db.write("INSERT INTO stock_movement(branch_id,ingredient_id,lot_id,kind,qty_base,ref_type,ref_id,created_by,operation_key,seq_no) VALUES(?,?,?,?,?,'invoice_line',?,?,?,?)",branch,ingredient,lot,kind,qty,line,actor,operation,seq);
    }
    @SuppressWarnings("unchecked")
    public void consume(Map<String,Object> order,Map<String,Object> line,UUID actor){
        UUID branch=OperationRepository.id(order,"branch_id"),lineId=OperationRepository.id(line,"id");db.advisory("stock:"+branch);
        if(!(line.get("options_snapshot") instanceof Map<?,?> snapshot)||!(snapshot.get("inventory_allocations") instanceof List<?> allocations))throw new ApiException(409,"Thiếu snapshot nguyên liệu; không được chế biến.");
        int seq=0;
        for(Object raw:allocations){Map<String,Object> a=(Map<String,Object>)raw;UUID lot=UUID.fromString(a.get("lot_id").toString()),ing=UUID.fromString(a.get("ingredient_id").toString());BigDecimal qty=OperationRepository.money(a.get("qty"));
            var l=db.lock("stock_lot",lot);
            if(l.get("expiry_date")!=null && LocalDate.parse(l.get("expiry_date").toString()).isBefore(LocalDate.now(java.time.ZoneId.of("Asia/Ho_Chi_Minh"))))throw new ApiException(409,"Lô đã hết hạn; cần xử lý giữ chỗ trước khi chế biến.");
            if(OperationRepository.money(l.get("reserved_qty")).compareTo(qty)<0||OperationRepository.money(l.get("qty_remaining")).compareTo(qty)<0)throw new ApiException(409,"Tồn và giữ chỗ không khớp.");
            db.write("UPDATE stock_lot SET reserved_qty=reserved_qty-?,qty_remaining=qty_remaining-? WHERE id=?",qty,qty,lot);
            movement(branch,ing,lot,"release",qty.negate(),lineId,actor,"start:"+lineId,seq++);movement(branch,ing,lot,"consume",qty.negate(),lineId,actor,"start:"+lineId,seq++);
        }
    }
    @SuppressWarnings("unchecked")
    public void release(Map<String,Object> order,Map<String,Object> line,UUID actor){
        UUID branch=OperationRepository.id(order,"branch_id"),lineId=OperationRepository.id(line,"id");db.advisory("stock:"+branch);int seq=0;
        if(line.get("options_snapshot") instanceof Map<?,?> snapshot && snapshot.get("inventory_allocations") instanceof List<?> allocations)for(Object raw:allocations){var a=(Map<String,Object>)raw;UUID lot=UUID.fromString(a.get("lot_id").toString());BigDecimal qty=OperationRepository.money(a.get("qty"));db.lock("stock_lot",lot);
            if(db.write("UPDATE stock_lot SET reserved_qty=reserved_qty-? WHERE id=? AND reserved_qty>=?",qty,lot,qty)!=1)throw new ApiException(409,"Giữ chỗ không khớp.");
            movement(branch,UUID.fromString(a.get("ingredient_id").toString()),lot,"release",qty.negate(),lineId,actor,"cancel:"+lineId,seq++);
        }
    }
}

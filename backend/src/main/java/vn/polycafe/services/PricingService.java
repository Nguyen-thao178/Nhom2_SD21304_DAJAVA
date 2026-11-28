package vn.polycafe.services;

import java.math.BigDecimal;
import java.util.*;
import org.springframework.stereotype.Service;
import vn.polycafe.api.WorkflowRequests.Line;
import vn.polycafe.common.ApiException;
import vn.polycafe.dao.OperationRepository;

@Service
public class PricingService {
    private final OperationRepository db;
    public record Quote(Map<String,Object> item,BigDecimal price,List<Map<String,Object>> options) {}
    public PricingService(OperationRepository db){this.db=db;}
    private BigDecimal rule(UUID branch,String channel,UUID item,UUID variant){
        var rows=db.rows("SELECT price FROM price_rule WHERE ((CAST(? AS uuid) IS NOT NULL AND item_id=?) OR (CAST(? AS uuid) IS NOT NULL AND variant_id=?)) AND (branch_id=? OR branch_id IS NULL) AND (channel=? OR channel IS NULL) AND valid_from<=CURRENT_TIMESTAMP AND (valid_to IS NULL OR valid_to>CURRENT_TIMESTAMP) ORDER BY branch_id NULLS LAST,channel NULLS LAST,valid_from DESC,id LIMIT 1",item,item,variant,variant,branch,channel);
        return rows.isEmpty()?null:OperationRepository.money(rows.get(0).get("price"));
    }
    public Quote quote(UUID branch,String channel,Line line){
        var item=db.one("SELECT * FROM menu_item WHERE id=? AND status='active' AND kind IN ('drink','food') AND (valid_from IS NULL OR valid_from<=CURRENT_TIMESTAMP) AND (valid_to IS NULL OR valid_to>CURRENT_TIMESTAMP)",line.itemId());
        if(item.get("branch_overrides") instanceof Map<?,?> overrides){Object override=overrides.get(branch.toString());if(override instanceof Map<?,?> value && value.get("status")!=null && !"active".equals(value.get("status")))throw new ApiException(409,"Món đã ngừng bán tại chi nhánh.");}
        BigDecimal price=rule(branch,channel,line.itemId(),null);if(price==null)throw new ApiException(409,"Món chưa có giá hiệu lực.");
        var options=new ArrayList<Map<String,Object>>();
        if(line.sizeId()!=null){var size=db.one("SELECT * FROM menu_variant WHERE id=? AND item_id=? AND kind='size' AND status='active'",line.sizeId(),line.itemId());BigDecimal sizePrice=rule(branch,channel,null,line.sizeId());price=sizePrice==null?price.add(OperationRepository.money(size.get("extra_price"))):sizePrice;}
        var seen=new HashSet<UUID>();var kinds=new HashSet<String>();
        for(UUID id:line.optionIds()==null?List.<UUID>of():line.optionIds()){
            if(!seen.add(id))throw new ApiException(400,"Tùy chọn bị lặp.");
            var option=db.one("SELECT * FROM menu_variant WHERE id=? AND (item_id=? OR item_id IS NULL) AND kind IN ('sugar','ice','topping') AND status='active'",id,line.itemId());
            String kind=option.get("kind").toString();if(!kind.equals("topping")&&!kinds.add(kind))throw new ApiException(400,"Chỉ chọn một mức đường và đá.");
            BigDecimal extra=rule(branch,channel,null,id);price=price.add(extra==null?OperationRepository.money(option.get("extra_price")):extra);options.add(option);
        }
        return new Quote(item,price.setScale(0,java.math.RoundingMode.UNNECESSARY),options);
    }
}

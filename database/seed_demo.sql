-- PolyCafe demo v1. UTF-8. Fictional data; no external payment/device calls.
-- Stable UUIDs and a transaction make reruns safe without overwriting existing rows.
BEGIN;
SELECT pg_advisory_xact_lock(hashtext('polycafe-demo-v1'));
DO $seed$
DECLARE
 b uuid := md5('polycafe-demo-v1:branch')::uuid;
 actor uuid; st uuid := md5('polycafe-demo-v1:station')::uuid;
 sh uuid; inv uuid; ln uuid; item uuid; ing uuid; unitid uuid; rec uuid;
 cust uuid; pager uuid; tick uuid; paid boolean; amount numeric;
 day date; moment timestamptz; n integer; i integer; j integer; d integer;
 names text[] := ARRAY['Cà phê đen đá','Cà phê sữa đá','Bạc xỉu','Americano','Latte','Cappuccino','Trà đào cam sả','Trà vải','Trà sen vàng','Matcha latte','Chocolate đá xay','Bánh croissant'];
 prices integer[] := ARRAY[25000,29000,35000,35000,45000,45000,39000,39000,45000,49000,49000,32000];
 ingredients text[] := ARRAY['Cà phê rang xay','Sữa đặc','Sữa tươi','Trà đen','Siro đào','Siro vải','Bột matcha','Bột chocolate','Bánh croissant đông lạnh','Đường'];
BEGIN
 SELECT id INTO STRICT actor FROM public.app_user WHERE username='admin1' AND status='active';
 IF EXISTS(SELECT 1 FROM public.branch WHERE id=b) THEN
   RAISE NOTICE 'Demo v1 already exists; no data changed'; RETURN;
 END IF;
 INSERT INTO public.branch(id,code,name,address,status,brand_name,business_day_cutoff,timezone,multi_station,layout_config)
 VALUES(b,'DEMO-PC01','PolyCafe - Chi nhánh mẫu','Địa chỉ minh họa, Thành phố Hồ Chí Minh','active','PolyCafe','04:00','Asia/Ho_Chi_Minh',false,'{"schema_version":1,"zones":[{"code":"A","name":"Tầng trệt"},{"code":"B","name":"Sân vườn"}]}');
 INSERT INTO public.station(id,branch_id,name,kind,status) VALUES(st,b,'Quầy pha chế mẫu','bar','active');
 INSERT INTO public.station(id,branch_id,name,kind,status) VALUES(md5('polycafe-demo-v1:kitchen')::uuid,b,'Quầy bánh mẫu','kitchen','active');
 FOR i IN 1..8 LOOP
  INSERT INTO public.dining_table(id,branch_id,code,seats,qr_token,status,zone_code)
  VALUES(md5('polycafe-demo-v1:table:'||i)::uuid,b,'DEMO-B'||lpad(i::text,2,'0'),CASE WHEN i<=4 THEN 2 ELSE 4 END,'polycafe-demo-v1-table-'||i,'free',CASE WHEN i<=4 THEN 'A' ELSE 'B' END);
 END LOOP;
 FOR i IN 1..6 LOOP
  INSERT INTO public.pager_device(id,branch_id,code,status,supports_remote)
  VALUES(md5('polycafe-demo-v1:pager:'||i)::uuid,b,'DEMO-R'||lpad(i::text,2,'0'),CASE WHEN i=1 THEN 'issued' WHEN i=2 THEN 'calling' ELSE 'ready' END,false);
 END LOOP;
 FOR i IN 1..10 LOOP
  INSERT INTO public.customer(id,member_code,kind,full_name,email,tier,points_balance,status)
  VALUES(md5('polycafe-demo-v1:customer:'||i)::uuid,'DEMO-KH'||lpad(i::text,3,'0'),'member',(ARRAY['Nguyễn An','Trần Bình','Lê Chi','Phạm Dũng','Hoàng Hà','Võ Linh','Đặng Mai','Bùi Nam','Đỗ Phương','Hồ Thảo'])[i]||' (mẫu)','demo.customer.'||i||'@example.invalid','standard',0,'active');
 END LOOP;
 FOR i IN 1..10 LOOP
  ing:=md5('polycafe-demo-v1:ingredient:'||i)::uuid; unitid:=md5('polycafe-demo-v1:unit:'||i)::uuid;
  INSERT INTO public.ingredient(id,name,base_unit,min_stock,shelf_life_days,status)
  VALUES(ing,ingredients[i]||' (mẫu)',CASE WHEN i=9 THEN 'cái' WHEN i IN (2,3,5,6) THEN 'ml' ELSE 'g' END,100,90,'active');
  INSERT INTO public.ingredient_unit(id,ingredient_id,name,to_base_qty) VALUES(unitid,ing,CASE WHEN i=9 THEN 'cái' WHEN i IN (2,3,5,6) THEN 'ml' ELSE 'g' END,1);
  INSERT INTO public.stock_lot(id,branch_id,ingredient_id,lot_code,expiry_date,qty_remaining,reserved_qty,unit_cost)
  VALUES(md5('polycafe-demo-v1:lot:'||i)::uuid,b,ing,'DEMO-LOT-'||i,(now() AT TIME ZONE 'Asia/Ho_Chi_Minh')::date+60,10000,0,CASE WHEN i=9 THEN 18000 ELSE 100 END);
  INSERT INTO public.stock_movement(id,branch_id,ingredient_id,lot_id,kind,qty_base,ref_type,ref_id,created_by,operation_key,seq_no)
  VALUES(md5('polycafe-demo-v1:receipt:'||i)::uuid,b,ing,md5('polycafe-demo-v1:lot:'||i)::uuid,'receipt',10000,'demo_opening',b,actor,'polycafe-demo-v1:receipt:'||i,0);
 END LOOP;
 FOR i IN 1..12 LOOP
  item:=md5('polycafe-demo-v1:item:'||i)::uuid;
  INSERT INTO public.menu_item(id,name,description,station_kind,status,category_code,category_name,kind)
  VALUES(item,names[i],'Dữ liệu mẫu phục vụ kiểm thử giao diện.',CASE WHEN i=12 THEN 'kitchen' ELSE 'bar' END,'active',CASE WHEN i<=6 THEN 'COFFEE' WHEN i<=9 THEN 'TEA' WHEN i<=11 THEN 'BLENDED' ELSE 'BAKERY' END,CASE WHEN i<=6 THEN 'Cà phê' WHEN i<=9 THEN 'Trà trái cây' WHEN i<=11 THEN 'Đá xay' ELSE 'Bánh ngọt' END,CASE WHEN i=12 THEN 'food' ELSE 'drink' END);
  INSERT INTO public.price_rule(id,item_id,price,valid_from) VALUES(md5('polycafe-demo-v1:price:'||i)::uuid,item,prices[i],now()-interval '30 days');
  INSERT INTO public.menu_variant(id,item_id,kind,name,extra_price,sort_order,status,stock_action)
  VALUES(md5('polycafe-demo-v1:size:'||i)::uuid,item,'size','M',0,1,'active','none');
  j:=CASE WHEN i<=6 THEN 1 WHEN i<=9 THEN 4 WHEN i=10 THEN 7 WHEN i=11 THEN 8 ELSE 9 END;
  rec:=md5('polycafe-demo-v1:recipe:'||i)::uuid;
  INSERT INTO public.recipe(id,item_id,version,status,effective_from) VALUES(rec,item,1,'active',now()-interval '30 days');
  INSERT INTO public.recipe_line(id,recipe_id,ingredient_id,quantity,unit_id)
  VALUES(md5('polycafe-demo-v1:recipe-line:'||i)::uuid,rec,md5('polycafe-demo-v1:ingredient:'||j)::uuid,CASE WHEN i=12 THEN 1 ELSE 20 END,md5('polycafe-demo-v1:unit:'||j)::uuid);
 END LOOP;
 FOR i IN 1..2 LOOP
  INSERT INTO public.supplier(id,name,contact,payment_terms_days,status)
  VALUES(md5('polycafe-demo-v1:supplier:'||i)::uuid,CASE WHEN i=1 THEN 'Nhà cung cấp cà phê mẫu' ELSE 'Nhà cung cấp sữa và bánh mẫu' END,'demo.supplier.'||i||'@example.invalid',15,'active');
 END LOOP;
 INSERT INTO public.promotion(id,name,kind,value,min_total,priority,stackable,code,max_uses,used_count,valid_from,valid_to,rules,campaign_key)
 VALUES(md5('polycafe-demo-v1:promo')::uuid,'Ưu đãi mẫu 10%','percent',10,100000,1,false,'DEMO10',100,0,now()-interval '1 day',now()+interval '30 days','{"schema_version":1}','polycafe-demo-v1');
 FOR d IN 0..6 LOOP
  day:=(now() AT TIME ZONE 'Asia/Ho_Chi_Minh')::date-d;
  moment:=(day+time '08:00') AT TIME ZONE 'Asia/Ho_Chi_Minh';
  sh:=md5('polycafe-demo-v1:shift:'||d)::uuid;
  INSERT INTO public.shift(id,branch_id,opened_by,opened_at,closed_at,opening_cash,status)
  VALUES(sh,b,actor,moment,CASE WHEN d=0 THEN NULL ELSE moment+interval '12 hours' END,500000,CASE WHEN d=0 THEN 'open' ELSE 'closed' END);
  INSERT INTO public.shift_assignment(id,shift_id,user_id,from_at,to_at,duty,zone_code)
  VALUES(md5('polycafe-demo-v1:assignment:'||d)::uuid,sh,actor,moment,CASE WHEN d=0 THEN NULL ELSE moment+interval '12 hours' END,'Thu ngân và quản lý ca (mẫu)','A');
  FOR n IN 1..3 LOOP
   i:=1+((d*3+n-1)%12); item:=md5('polycafe-demo-v1:item:'||i)::uuid;
   inv:=md5('polycafe-demo-v1:invoice:'||d||':'||n)::uuid; ln:=md5('polycafe-demo-v1:line:'||d||':'||n)::uuid;
   amount:=prices[i]*2;
   INSERT INTO public.invoice(id,branch_id,invoice_no,source,staff_id,customer_id,channel,status,subtotal,discount_total,total,paid_total,idempotency_key,confirmed_by,confirmed_at,business_date,created_shift_id,responsible_shift_id,discount_snapshot,created_at)
   VALUES(inv,b,'DEMO-'||to_char(day,'YYYYMMDD')||'-'||n,'staff',actor,md5('polycafe-demo-v1:customer:'||(1+((d+n)%10)))::uuid,'counter','paid',amount,0,amount,amount,'polycafe-demo-v1:invoice:'||d||':'||n,actor,moment+interval '1 hour',day,sh,sh,'{"schema_version":1}',moment+interval '1 hour');
   INSERT INTO public.invoice_line(id,invoice_id,item_id,quantity,unit_price,options_snapshot,status,station_id,recipe_id,line_kind,handed_qty,accepted_by,accepted_at,prepared_by,started_at,ready_by,ready_at)
   VALUES(ln,inv,item,2,prices[i],'{"schema_version":1}','handed_over',CASE WHEN i=12 THEN md5('polycafe-demo-v1:kitchen')::uuid ELSE st END,md5('polycafe-demo-v1:recipe:'||i)::uuid,'sale',2,actor,moment+interval '61 minutes',actor,moment+interval '62 minutes',actor,moment+interval '65 minutes');
   INSERT INTO public.payment(id,invoice_id,shift_id,method,amount,status,tx_ref,confirmed_by,paid_at,kind,confirm_note)
   VALUES(md5('polycafe-demo-v1:payment:'||d||':'||n)::uuid,inv,sh,CASE WHEN n=1 THEN 'cash' WHEN n=2 THEN 'transfer' ELSE 'card' END,amount,'succeeded','DEMO-TX-'||d||'-'||n,actor,moment+interval '66 minutes','collect','Giao dịch giả lập, không thu tiền thật');
   INSERT INTO public.ticket(id,branch_id,invoice_id,ticket_no,status,issued_by,business_date,issued_at,closed_at,returned_at)
   VALUES(md5('polycafe-demo-v1:ticket:'||d||':'||n)::uuid,b,inv,'DEMO-'||n,'closed',actor,day,moment+interval '1 hour',moment+interval '67 minutes',moment+interval '67 minutes');
   INSERT INTO public.fulfillment_event(id,invoice_id,line_id,ticket_id,kind,quantity,actor_id,receiver_label,result,request_key,note,created_at)
   VALUES(md5('polycafe-demo-v1:handover:'||d||':'||n)::uuid,inv,ln,md5('polycafe-demo-v1:ticket:'||d||':'||n)::uuid,'customer_handover',2,actor,'Khách mẫu','succeeded','polycafe-demo-v1:handover:'||d||':'||n,'Bàn giao giả lập',moment+interval '67 minutes');
   j:=CASE WHEN i<=6 THEN 1 WHEN i<=9 THEN 4 WHEN i=10 THEN 7 WHEN i=11 THEN 8 ELSE 9 END;
   INSERT INTO public.invoice_line_ingredient(id,line_id,ingredient_id,qty_base,recipe_qty_base)
   VALUES(md5('polycafe-demo-v1:snapshot:'||d||':'||n)::uuid,ln,md5('polycafe-demo-v1:ingredient:'||j)::uuid,CASE WHEN i=12 THEN 2 ELSE 40 END,CASE WHEN i=12 THEN 2 ELSE 40 END);
   INSERT INTO public.stock_movement(id,branch_id,ingredient_id,lot_id,kind,qty_base,ref_type,ref_id,created_by,operation_key,seq_no)
   VALUES(md5('polycafe-demo-v1:consume:'||d||':'||n)::uuid,b,md5('polycafe-demo-v1:ingredient:'||j)::uuid,md5('polycafe-demo-v1:lot:'||j)::uuid,'consume',CASE WHEN i=12 THEN -2 ELSE -40 END,'invoice_line',ln,actor,'polycafe-demo-v1:consume:'||d||':'||n,0);
   UPDATE public.stock_lot SET qty_remaining=qty_remaining-CASE WHEN i=12 THEN 2 ELSE 40 END WHERE id=md5('polycafe-demo-v1:lot:'||j)::uuid;
  END LOOP;
  SELECT 500000+coalesce(sum(p.amount),0) INTO amount FROM public.payment p WHERE p.shift_id=sh AND p.method='cash';
  UPDATE public.shift SET expected_cash=amount,counted_cash=CASE WHEN d=0 THEN NULL ELSE amount END,difference=CASE WHEN d=0 THEN NULL ELSE 0 END WHERE id=sh;
 END LOOP;
 -- Three current orders demonstrate waiting, ready/calling and draft states.
 FOR n IN 1..3 LOOP
  inv:=md5('polycafe-demo-v1:live-invoice:'||n)::uuid; ln:=md5('polycafe-demo-v1:live-line:'||n)::uuid;
  day:=(now() AT TIME ZONE 'Asia/Ho_Chi_Minh')::date; sh:=md5('polycafe-demo-v1:shift:0')::uuid;
  INSERT INTO public.invoice(id,branch_id,invoice_no,source,staff_id,customer_id,channel,status,subtotal,discount_total,total,paid_total,idempotency_key,business_date,created_shift_id,responsible_shift_id,discount_snapshot,confirmed_by,confirmed_at)
  VALUES(inv,b,'DEMO-LIVE-'||n,'staff',actor,md5('polycafe-demo-v1:customer:'||n)::uuid,'takeaway',CASE WHEN n=3 THEN 'draft' ELSE 'confirmed' END,prices[n],0,prices[n],0,'polycafe-demo-v1:live:'||n,day,sh,sh,'{"schema_version":1}',CASE WHEN n=3 THEN NULL ELSE actor END,CASE WHEN n=3 THEN NULL ELSE now() END);
  INSERT INTO public.invoice_line(id,invoice_id,item_id,quantity,unit_price,options_snapshot,status,line_kind,handed_qty,station_id,recipe_id,accepted_by,accepted_at,prepared_by,started_at,ready_by,ready_at)
  VALUES(ln,inv,md5('polycafe-demo-v1:item:'||n)::uuid,1,prices[n],'{"schema_version":1}',CASE WHEN n=1 THEN 'waiting' WHEN n=2 THEN 'ready' ELSE 'pending_accept' END,'sale',0,st,md5('polycafe-demo-v1:recipe:'||n)::uuid,CASE WHEN n=2 THEN actor END,CASE WHEN n=2 THEN now() END,CASE WHEN n=2 THEN actor END,CASE WHEN n=2 THEN now() END,CASE WHEN n=2 THEN actor END,CASE WHEN n=2 THEN now() END);
  IF n<=2 THEN
   tick:=md5('polycafe-demo-v1:live-ticket:'||n)::uuid; pager:=md5('polycafe-demo-v1:pager:'||n)::uuid;
   INSERT INTO public.ticket(id,branch_id,invoice_id,ticket_no,pager_id,status,issued_by,business_date,issued_at)
   VALUES(tick,b,inv,'DEMO-LIVE-'||n,pager,CASE WHEN n=2 THEN 'ready' ELSE 'open' END,actor,day,now());
   IF n=2 THEN
    INSERT INTO public.fulfillment_event(id,invoice_id,line_id,ticket_id,pager_id,kind,quantity,actor_id,result,request_key,note)
    VALUES(md5('polycafe-demo-v1:call')::uuid,inv,ln,tick,pager,'call',0,actor,'succeeded','polycafe-demo-v1:call','Mô phỏng rung thiết bị, không gửi lệnh tới phần cứng');
    INSERT INTO public.invoice_line_ingredient(id,line_id,ingredient_id,qty_base,recipe_qty_base) VALUES(md5('polycafe-demo-v1:live-snapshot')::uuid,ln,md5('polycafe-demo-v1:ingredient:1')::uuid,20,20);
    INSERT INTO public.stock_movement(id,branch_id,ingredient_id,lot_id,kind,qty_base,ref_type,ref_id,created_by,operation_key,seq_no)
    VALUES(md5('polycafe-demo-v1:live-consume')::uuid,b,md5('polycafe-demo-v1:ingredient:1')::uuid,md5('polycafe-demo-v1:lot:1')::uuid,'consume',-20,'invoice_line',ln,actor,'polycafe-demo-v1:live-consume',0);
    UPDATE public.stock_lot SET qty_remaining=qty_remaining-20 WHERE id=md5('polycafe-demo-v1:lot:1')::uuid;
   END IF;
  END IF;
 END LOOP;
 INSERT INTO public.purchase_order(id,branch_id,supplier_id,status,requested_by,total_amount,paid_amount,due_date)
 VALUES(md5('polycafe-demo-v1:po')::uuid,b,md5('polycafe-demo-v1:supplier:1')::uuid,'requested',actor,300000,0,(now() AT TIME ZONE 'Asia/Ho_Chi_Minh')::date+7);
 INSERT INTO public.purchase_order_line(id,purchase_order_id,ingredient_id,unit_id,qty_ordered,qty_received,qty_rejected,unit_price)
 VALUES(md5('polycafe-demo-v1:po-line')::uuid,md5('polycafe-demo-v1:po')::uuid,md5('polycafe-demo-v1:ingredient:1')::uuid,md5('polycafe-demo-v1:unit:1')::uuid,1000,0,0,300);
 INSERT INTO public.audit_log(id,branch_id,actor_id,action,entity_type,entity_id,reason,request_id)
 VALUES(md5('polycafe-demo-v1:audit')::uuid,b,actor,'demo_seed','branch',b,'Tạo dữ liệu minh họa theo yêu cầu; không có giao dịch thật','polycafe-demo-v1');
END $seed$;
COMMIT;

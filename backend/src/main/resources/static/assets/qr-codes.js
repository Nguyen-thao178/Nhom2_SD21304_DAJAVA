"use strict";
(() => {
 const context=document.querySelector('meta[name="app-context"]').content;
 const el=(tag,text)=>{const e=document.createElement(tag);if(text!==undefined)e.textContent=text;return e;};
 async function api(path){const r=await fetch(context+path);if(!r.ok)throw new Error('Không thể tải dữ liệu. Hãy kiểm tra đăng nhập và quyền chi nhánh.');return r.json();}
 async function orders(){const rows=await api('/api/staff/qr-orders');const box=document.querySelector('#qr-orders');box.replaceChildren();if(!rows.length)box.append(el('p','Chưa có yêu cầu QR.'));for(const r of rows)box.append(el('p',r.invoice_no+' · '+r.table_code+' · '+r.full_name+' · '+new Intl.NumberFormat('vi-VN',{style:'currency',currency:'VND'}).format(r.total)+' · '+(r.status==='draft'?'Chờ xác nhận':r.status)));}
 document.querySelector('#print-qr').addEventListener('click',()=>window.print());
 document.querySelector('#refresh-qr-orders').addEventListener('click',()=>orders().catch(e=>{document.querySelector('#qr-error').textContent=e.message;}));
 (async()=>{try{for(const t of await api('/api/staff/qr-tables')){const card=el('article');card.className='customer-product qr-card';const image=el('img');image.className='qr-image';image.alt='Mã QR bàn '+t.code;image.src=context+'/api/staff/qr-tables/'+encodeURIComponent(t.id)+'/image';const link=el('a','Mở thực đơn bàn này');link.href=context+'/qr/'+encodeURIComponent(t.qr_token);card.append(el('small',t.branch_name),el('h2',t.code),image,el('p','Quét để chọn món · PolyCafe'),link);document.querySelector('#qr-cards').append(card);}await orders();}catch(e){document.querySelector('#qr-error').textContent=e.message;}})();
})();

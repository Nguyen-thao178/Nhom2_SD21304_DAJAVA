<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html><html lang="vi"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<meta name="app-context" content="<c:out value='${pageContext.request.contextPath}'/>">
<title>Thành viên &amp; gọi món QR · PolyCafe</title>
<link rel="stylesheet" href="<c:url value='/assets/cafe.css'/>"><link rel="stylesheet" href="<c:url value='/assets/customer.css'/>">
<script defer src="<c:url value='/assets/customer.js'/>"></script></head>
<body class="customer-page"><header class="customer-header"><a class="brand" href="<c:url value='/customer'/>">P. PolyCafe</a><span>Chọn món. Thảnh thơi tận hưởng.</span></header>
<main class="customer-layout"><section class="customer-hero"><span class="eyebrow">POLYCAFE MEMBERS</span><h1>Một lần quét.<br>Một ly yêu thích.</h1><p>Quét mã trên bàn để xem thực đơn. Đăng nhập thành viên để gửi yêu cầu món và theo dõi đơn của bạn.</p></section>
<div id="customer-message" class="notice" role="status" hidden></div>
<section class="card customer-card"><h2>Tài khoản thành viên</h2><div id="member-info" hidden><p id="member-summary"></p><button id="member-logout" class="button secondary">Đăng xuất</button></div>
<form id="member-login"><label>Tên đăng nhập<input id="member-username" autocomplete="username" maxlength="60" required></label><label>Mật khẩu<input id="member-password" type="password" autocomplete="current-password" maxlength="72" required></label><button class="button primary">Đăng nhập thành viên</button></form></section>
<section class="card customer-card"><h2>Quét mã QR tại bàn</h2><p class="muted">Dùng camera điện thoại quét mã trên bàn, hoặc mở camera bên dưới. Camera trong trang cần HTTPS (localhost dùng được).</p>
<div class="scan-actions"><button id="start-scan" class="button primary">Mở camera</button><button id="stop-scan" class="button secondary" hidden>Dừng camera</button></div><video id="qr-camera" playsinline muted hidden></video>
<form id="qr-form"><label>Liên kết QR hoặc mã bàn<input id="qr-input" maxlength="500" placeholder="Dán liên kết QR hoặc mã do quán cung cấp" required></label><button class="button secondary">Xem thực đơn</button></form></section>
<section id="qr-menu" hidden><div class="page-heading"><div><span class="eyebrow">THỰC ĐƠN TẠI BÀN</span><h2 id="table-label"></h2></div></div><div id="customer-products" class="customer-products"></div></section>
<section id="customer-cart" class="card customer-card" hidden><h2>Món bạn chọn</h2><div id="cart-lines"></div><p id="cart-total"></p><label>Ghi chú<input id="order-note" maxlength="300" placeholder="Ví dụ: ít ngọt, không đá"></label><p class="muted">Giá cuối cùng được kiểm tra trên server. Nhân viên sẽ xác nhận yêu cầu trước khi chế biến.</p><button id="send-order" class="button primary">Gửi yêu cầu món</button></section>
<section id="member-orders" class="card customer-card" hidden><h2>Đơn gần đây của bạn</h2><button id="refresh-orders" class="button secondary">Tải lại</button><div id="order-history"></div></section>
</main><footer class="customer-footer">PolyCafe · Chăm chút từng trải nghiệm</footer></body></html>

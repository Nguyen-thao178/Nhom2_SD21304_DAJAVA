<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html><html lang="vi"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1"><meta name="app-context" content="<c:out value='${pageContext.request.contextPath}'/>">
<title>Mã QR theo bàn · PolyCafe</title><link rel="stylesheet" href="<c:url value='/assets/cafe.css'/>"><link rel="stylesheet" href="<c:url value='/assets/customer.css'/>"><script defer src="<c:url value='/assets/qr-codes.js'/>"></script></head>
<body class="customer-page"><main class="customer-layout"><div class="page-heading"><div><a href="<c:url value='/app/overview'/>">← Không gian vận hành</a><h1>QR tại bàn</h1><p>Chỉ hiển thị bàn trong chi nhánh được cấp quyền.</p></div><button id="print-qr" class="button primary">In mã QR</button></div>
<p class="notice">Khi dùng điện thoại, cấu hình app.public-base-url bằng địa chỉ HTTPS mà điện thoại truy cập được rồi khởi động lại server. QR mặc định dùng localhost dành cho kiểm thử trên máy này.</p>
<p id="qr-error" role="status"></p><div id="qr-cards" class="customer-products"></div>
<section class="card customer-card no-print"><h2>Yêu cầu món từ khách QR</h2><button id="refresh-qr-orders" class="button secondary">Tải lại</button><div id="qr-orders"></div><p class="muted">Đơn nháp chờ xác nhận. Trang này chỉ theo dõi yêu cầu; không tự thu tiền hay gửi chế biến.</p></section></main></body></html>

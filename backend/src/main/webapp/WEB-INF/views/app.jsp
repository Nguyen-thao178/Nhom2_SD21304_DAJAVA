<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="vi">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<meta name="app-context" content="<c:out value='${pageContext.request.contextPath}'/>">
<title>
<c:out value="${page.title}"/> · PolyCafe</title>
<link rel="stylesheet" href="<c:url value='/assets/cafe.css'/>">
<script defer src="<c:url value='/assets/cafe.js'/>">
</script>
</head>
<body data-screen="<c:out value='${page.path}'/>" data-resource="<c:out value='${page.resource}'/>" data-manager="${isManager}">
<a href="#main" class="skip-link">Đến nội dung chính</a>
<aside class="sidebar" id="sidebar">
<a class="brand" href="<c:url value='/app/overview'/>">
<span class="brand-mark">P.</span> PolyCafe</a>
<div class="workspace-label">
<span class="status-dot">
</span> Không gian vận hành</div>
<nav aria-label="Điều hướng chính">
<a class="nav-link" href="<c:url value='/app/qr-codes'/>">QR tại bàn &amp; yêu cầu món</a>
<c:set var="previousGroup" value=""/>
<c:forEach items="${screens}" var="nav">
<c:if test="${nav.group ne previousGroup}">
<div class="nav-group">
<c:out value="${nav.group}"/>
</div>
<c:set var="previousGroup" value="${nav.group}"/>
</c:if>
<a class="nav-link ${page.path eq nav.path ? 'active' : ''}" href="<c:url value='/app/${nav.path}'/>" <c:if test="${page.path eq nav.path}">aria-current="page"</c:if>>
<span class="nav-symbol" aria-hidden="true">${nav.path eq 'overview' ? '◫' : nav.path eq 'pos' ? '+' : nav.path eq 'reports' ? '▥' : '○'}</span>
<c:out value="${nav.title}"/>
</a>
</c:forEach>
</nav>
<div class="sidebar-bottom">
<div class="avatar">P</div>
<div>
<strong>
<c:out value="${staffName}"/>
</strong>
<small>
<c:out value="${staffUsername}"/>
</small>
</div>
<button id="logout" class="icon-button" title="Đăng xuất" aria-label="Đăng xuất">↗</button>
</div>
</aside>
<div class="app-layout">
<header class="topbar">
<button id="menu-toggle" class="icon-button mobile-only" aria-label="Mở menu" aria-expanded="false">☰</button>
<div class="breadcrumb">Không gian quản lý <span>/</span>
<strong>
<c:out value="${page.title}"/>
</strong>
</div>
<div class="topbar-right">
<span class="today" id="today">
</span>
<span class="user-chip">
<c:out value="${staffName}"/>
</span>
</div>
</header>
<main id="main" class="main-content">
<div class="page-heading">
<div>
<span class="eyebrow">POLYCAFE WORKSPACE</span>
<h1>
<c:out value="${page.title}"/>
</h1>
<p class="muted">
<c:out value="${page.subtitle}"/>
</p>
</div>
<button class="button secondary" id="refresh-page">↻ <span>Làm mới</span>
</button>
</div>
<div id="page-message" class="notice" role="status" hidden>
</div>
<c:choose>
<c:when test="${page.path eq 'overview'}">
<%@ include file="partials/overview.jspf" %>
</c:when>
<c:when test="${page.path eq 'pos'}">
<%@ include file="partials/pos.jspf" %>
</c:when>
<c:when test="${page.path eq 'menu'}">
<%@ include file="partials/menu.jspf" %>
</c:when>
<c:when test="${page.path eq 'reports'}">
<%@ include file="partials/reports.jspf" %>
</c:when>
<c:otherwise>
<%@ include file="partials/records.jspf" %>
</c:otherwise>
</c:choose>
</main>
<footer class="app-footer">PolyCafe · Chăm chút từng trải nghiệm</footer>
</div>
<dialog id="editor-dialog" class="editor-dialog" aria-labelledby="editor-title">
<form id="editor-form">
<div class="dialog-heading">
<div>
<span class="eyebrow">QUẢN LÝ DỮ LIỆU</span>
<h2 id="editor-title">Thông tin</h2>
</div>
<button type="button" class="icon-button close-dialog" aria-label="Đóng">×</button>
</div>
<div id="editor-fields" class="form-grid">
</div>
<p id="editor-error" class="form-error" role="alert" hidden>
</p>
<div class="dialog-footer">
<button type="button" class="button secondary close-dialog">Hủy</button>
<button type="submit" class="button primary">Lưu thay đổi</button>
</div>
</form>
</dialog>
<dialog id="confirm-dialog" class="confirm-dialog" aria-labelledby="confirm-title">
<h2 id="confirm-title">Xác nhận thao tác</h2>
<p id="confirm-copy">
</p>
<div class="dialog-footer">
<button type="button" class="button secondary" id="confirm-cancel">Quay lại</button>
<button type="button" class="button danger" id="confirm-ok">Xác nhận</button>
</div>
</dialog>
<div id="toast" class="toast" role="status" hidden>
</div>
</body>
</html>

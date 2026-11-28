<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>
        <!doctype html>
        <html lang="vi">

        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1">
            <meta name="app-context" content="<c:out value='${pageContext.request.contextPath}'/>">
            <title>Đăng nhập · PolyCafe</title>
            <link rel="stylesheet" href="<c:url value='/assets/cafe.css'/>">
            <script defer src="<c:url value='/assets/cafe.js'/>">
            </script>
        </head>

        <body class="login-page" data-screen="login">
            <section class="login-story" aria-label="PolyCafe">
                <a class="brand brand-light" href="<c:url value='/login'/>">
                    <span class="brand-mark">P.</span> PolyCafe</a>
                <div class="story-content">
                    <span class="eyebrow">MỖI NGÀY, MỘT TRẢI NGHIỆM TỐT HƠN</span>
                    <h1>Một quán cà phê.<br>Nhiều điều kết nối.</h1>
                    <p>Từ ly cà phê đầu ngày đến lần bàn giao cuối ca.<br>Cùng chăm chút cho từng bước phục vụ.</p>
                    <div class="coffee-art" aria-hidden="true">
                        <div class="coffee-saucer">
                        </div>
                        <div class="coffee-cup">
                            <i>
                            </i>
                        </div>
                        <div class="coffee-label">Brew. Serve. Connect.</div>
                    </div>
                </div>
                <p class="story-footer">KHÔNG GIAN LÀM VIỆC CỦA ĐỘI NGŨ POLYCAFE</p>
            </section>
            <main class="login-panel">
                <div class="login-box">
<p><a href="<c:url value='/customer'/>">Thành viên · QR →</a></p>
                    <span class="eyebrow">CHÀO MỪNG TRỞ LẠI</span>
                    <h2>Bắt đầu một ca mới.</h2>
                    <p class="muted">Đăng nhập bằng tài khoản được cấp cho bạn.</p>
                    <form id="login-form">
                        <label for="username">Tên đăng nhập</label>
                        <input id="username" name="username" autocomplete="username" maxlength="60" required autofocus
                            placeholder="Nhập tên đăng nhập">
                        <label for="password">Mật khẩu</label>
                        <div class="password-field">
                            <input id="password" name="password" type="password" autocomplete="current-password"
                                maxlength="72" required placeholder="Nhập mật khẩu">
                            <button type="button" id="toggle-password" class="text-button"
                                aria-label="Hiện mật khẩu">Hiện</button>
                        </div>
                        <p id="login-error" class="form-error" role="alert" hidden>
                        </p>
                        <button class="button primary full" type="submit">Đăng nhập <span aria-hidden="true">→</span>
                        </button>
                    </form>
                    <p class="login-help">Cần hỗ trợ tài khoản? Liên hệ quản lý quán.</p>
                </div>
                <small>PolyCafe · Không gian quản lý quán cà phê</small>
            </main>
        </body>

        </html>
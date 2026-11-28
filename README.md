# PolyCafe API

Trang thành viên `/customer`, trang QR theo bàn `/app/qr-codes`. Hướng dẫn tài khoản mẫu, phân quyền và cấu hình URL cho điện thoại: [QR_MEMBERS.md](docs/QR_MEMBERS.md).

Java 21, Spring Boot 3.5.7, Spring JDBC, PostgreSQL Supabase, Tomcat nhúng hoặc Apache Tomcat 10.1 qua WAR. Mã nguồn tại `backend/src/main/java/vn/polycafe`. Giữ các API sản phẩm và đăng nhập cũ.

## Kết nối và chạy trong VS Code

1. Mở thư mục `PolyCafe` trong VS Code, dùng JDK 21 trở lên và Maven.
2. Supabase PolyCafe → Connect → lấy JDBC URL Direct hoặc Session pooler 5432. Có `sslmode=require`; có thể dùng `verify-full` khi đã cấu hình CA. Direct cần mạng hỗ trợ IPv6. Không đoán tên host pooler.
3. Chọn Run and Debug → `PolyCafe API - Supabase`, nhập URL, DB user và mật khẩu PostgreSQL tại hộp nhập được che. Dữ liệu này không được ghi vào repository.
4. DB hiện chưa có global owner/sysadmin. Lần chạy đầu nhập username và password bootstrap 12–72 byte UTF-8. Bootstrap chỉ tạo nếu username chưa tồn tại. Các lần sau để trống hai mục bootstrap.
5. API mặc định `http://localhost:8080`. `/api/v1/database/status` xác minh JDBC sau khi đăng nhập.

Chạy terminal bằng `mvn test` và `mvn package` trong `backend`. File WAR có thể chạy với `java -jar target/polycafe-api-0.1.0.war` khi đã đặt `SUPABASE_DB_URL`, `SUPABASE_DB_USER`, `SUPABASE_DB_PASSWORD`. Môi trường HTTPS đặt `COOKIE_SECURE=true`. Thông tin mẫu trong `backend/application-local.properties.example`. File thật `application-local.properties` đã được gitignore.

Kết nối MCP đã đọc được schema PolyCafe. Kết nối JDBC trực tiếp từ ứng dụng cần mật khẩu PostgreSQL do người vận hành nhập. Không dùng publishable key hoặc service-role key thay mật khẩu JDBC.

## Gọi API

GET `/api/auth/csrf`, giữ cookie session và lấy `token`/`headerName`. POST `/api/auth/login` với JSON username/password cùng cookie và header CSRF. Tiếp tục giữ cookie; mọi POST/PATCH/DELETE cần token CSRF. Login đổi session ID. GET `/api/auth/me`, POST `/api/auth/logout` theo cơ chế hiện có.

Mỗi bảng có một controller riêng dưới `/api/v1/<tên-bảng-đổi-gạch-dưới-thành-gạch-ngang>`.

- GET collection nhận `after` UUID và `size` 1–100, sắp xếp theo UUID.
- GET `/{id}` đọc một bản ghi.
- GET `/schema` trả kiểu, cột, CHECK và thông tin nullable/default trong schema thực tế.
- POST collection nhận object JSON với tên cột snake_case, sinh UUID ở server.
- PATCH `/{id}` chỉ cập nhật trường gửi lên. Header `X-Expected-Updated-At` là giá trị `updated_at` từ lần GET gần nhất.
- DELETE `/{id}` dùng cùng header. Danh mục chuyển ngừng hoạt động, draft bị hủy, cấu hình chưa có tham chiếu có thể xóa theo FK RESTRICT.

Ví dụ POST `/api/v1/ingredient` với `{"name":"Sữa tươi","base_unit":"ml","status":"active"}`. PATCH body `{"name":"Sữa tươi đã sửa"}`. Đọc `/api/v1/database/catalog` cho danh sách đầy đủ. OpenAPI tại `/openapi.json` sau đăng nhập, có thể nhập file vào Postman.

## Quyền và toàn vẹn

Các endpoint CRUD quản trị mới giới hạn global owner/sysadmin, kiểm lại quyền từ DB mỗi request. Nhân viên tiếp tục sử dụng API sản phẩm hiện có. Mã này là API quản trị dữ liệu theo vòng đời; chưa thay thế toàn bộ API vận hành theo vai trò và 44 Use Case.

Schema trực tiếp ngày 21/11/2026 có 39 bảng, gồm 38 bảng SRS 2.0 và `invoice_line_ingredient` đã có trên Supabase. Không sửa UC hoặc tài liệu SRS.

`audit_log`, `point_ledger`, `stock_movement`, `fulfillment_event` là append-only. PATCH/DELETE trả 405. `invoice_line_ingredient` là snapshot hệ thống, POST/PATCH/DELETE trực tiếp bị chặn. Nhật ký được sinh cùng transaction CRUD. Nội dung before/after và payload kỹ thuật được ẩn khỏi API CRUD tổng quát.

Sổ điểm chỉ cho POST `adjust`, khóa khách thành viên và cập nhật số dư trong cùng transaction. Bút toán kho yêu cầu lô hợp lệ, kiểm dấu và tồn/giữ chỗ rồi cập nhật cache và sổ cùng transaction. Giao món cuối cập nhật số đã giao và chặn giao vượt. Khóa unique ở DB xử lý gửi trùng. Các workflow tự giữ chỗ, consume kèm release, thu/hoàn tiền thật, xác nhận đơn, duyệt chứng từ, điều chuyển và đóng ca cần service nghiệp vụ chuyên biệt; CRUD từ chối sửa chứng từ đã chốt hoặc nhảy trạng thái để bảo toàn dữ liệu.

Không cho client ghi hash, thời gian hệ thống, người thực hiện hoặc các số dư tính toán. Mật khẩu mới gửi qua trường `password`, được BCrypt băm, giới hạn 12–72 byte UTF-8. Hash/PIN và tham chiếu secret không được trả về. Chặn sửa quyền/tài khoản của chính người đang quản trị qua CRUD.

SQL chỉ ghép định danh từ allowlist đã đóng gói, mọi giá trị bind bằng PreparedStatement. Body JSON giới hạn 1 MiB, JSONB 256 KiB, độ sâu 30, từ chối key JSON trùng; endpoint ghi chỉ nhận JSON, không có XML parser. Giới hạn login theo IP 15 lần/phút và khóa tài khoản 15 phút sau 5 lần sai. Bộ giới hạn IP hiện ở bộ nhớ từng instance; triển khai nhiều instance cần rate limit chung ở gateway. React/React Native phải dùng cùng cookie/CSRF hoặc bổ sung cơ chế token được thiết kế riêng.

## Kiểm thử

`mvn test` kiểm tra HTTP CRUD thật với H2, SQL injection được lưu như dữ liệu, JSON/type validation, 401/403, CSRF, từ chối XML, stale write, quyền quản trị, bảng bất biến và tồn âm. H2 thay advisory lock bằng alias test, không chứng minh khả năng khóa/concurrency PostgreSQL. Chưa có kiểm thử JDBC live tới Supabase khi thiếu DB password; chưa đo tải hoặc kiểm thử penetration đầy đủ. Snapshot `schema-columns.json`, `schema-constraints.json` và `crud-catalog.json` được lấy từ database, chỉ chứa metadata.


## Giao diện JSP

Mở http://localhost:8080/login sau khi khởi động. Menu gồm tổng quan, bán hàng, đơn, bàn, quầy chế biến, ticket, thực đơn, kho, mua hàng, khách, ca, báo cáo và thiết lập. Các màn hình quản lý dùng biểu mẫu theo schema cho 39 bảng, có tìm kiếm trên phần dữ liệu đã tải, phân trang, chi tiết, sửa và ngừng/xóa theo quy tắc backend. Các bảng chỉ thêm chỉ có xem và thêm bút toán được phép.

Quầy bán hàng cho phép tìm món, soạn giỏ, chọn chi nhánh, lưu toàn bộ đơn nháp trong một transaction và in phiếu nháp. Server tính lại giá theo chi nhánh, ghi người tạo và chống gửi lặp cùng requestId. Quyền lưu nháp hiện dùng owner/sysadmin toàn dự án. Xác nhận đơn, gửi pha chế, điều khiển rung và thu/hoàn tiền thật vẫn cần các workflow backend chuyên biệt, giao diện không giả lập các kết quả này.

Báo cáo dùng dữ liệu SQL tổng hợp theo ngày kinh doanh, có khoảng ngày, biểu đồ và tải CSV. Màn hình tổng quan chỉ đếm tối đa 100 bản ghi đã tải và ghi rõ phạm vi. Mọi chữ từ database được render bằng textContent hoặc c:out, API ghi tiếp tục dùng CSRF, phiên đăng nhập và kiểm quyền.

Build bằng mvn package tạo WAR chạy được với java -jar hoặc triển khai lên Apache Tomcat 10.1 tương thích Jakarta. Khi deploy tên context khác, các đường dẫn giao diện/API lấy theo contextPath. src/main/webapp/WEB-INF/views chứa JSP, src/main/resources/static/assets chứa CSS và JavaScript. Không truy cập JSP trực tiếp trong WEB-INF.

Kiểm thử bổ sung dùng Tomcat/Jasper thật để biên dịch tất cả màn hình, kiểm contextPath, redirect đăng nhập và escaping tên tài khoản; kiểm lưu đơn nháp, retry không trùng và rollback khi món không hợp lệ trên H2. Kết nối production Supabase vẫn cần mật khẩu PostgreSQL do người vận hành nhập.

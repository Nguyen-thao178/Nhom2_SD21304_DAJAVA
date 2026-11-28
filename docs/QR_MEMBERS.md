# Thành viên và QR tại bàn

Trang khách: `/customer`. QR bàn mở `/qr/{qr_token}` để xem thực đơn công khai. Khách đăng nhập thành viên mới gửi được yêu cầu món; giá được lấy lại từ database theo chi nhánh, kênh QR và thời gian hiệu lực. Đơn lưu ở trạng thái `draft`, dòng món `pending_accept`, không tự thanh toán, giữ kho hoặc gửi lệnh pha chế. Yêu cầu trùng requestId chỉ trả lại cùng đơn; thay nội dung với cùng mã trả 409. Nhân viên tiếp nhận yêu cầu theo luồng xác nhận nghiệp vụ riêng.

Trang nhân viên: `/app/qr-codes`, có ảnh QR để in và danh sách yêu cầu QR trong chi nhánh được cấp. Thành viên bị chặn toàn bộ `/app`, API sản phẩm nội bộ, CRUD và báo cáo; chỉ đọc hồ sơ và 50 đơn gần nhất của chính mình. Không thay đổi UC, bảng hoặc tài khoản quản trị hiện có.

Tài khoản mẫu `nhanvien1` có role staff tại DEMO-PC01, `thanhvien1` có role customer và hồ sơ member riêng. Mật khẩu BCrypt trong database; mật khẩu ban đầu ngẫu nhiên nằm ở `backend/demo-accounts.local.txt` đã được gitignore. Có thể chạy lại công cụ thủ công từ thư mục backend:

```powershell
java --class-path 'target/polycafe-api-0.1.0/WEB-INF/lib/*' tools/ProvisionDemoAccounts.java
```

Công cụ không chạy khi khởi động server và không thay mật khẩu hay quyền của tài khoản đã tồn tại. File chứa mật khẩu ban đầu cần giữ riêng, không tải lên GitHub.

Đặt trong `application-local.properties` hoặc biến môi trường Spring:

```properties
app.public-base-url=https://your-cafe.example
```

Nếu deploy theo context `/polycafe`, thêm context đó vào URL. QR mặc định dùng `http://localhost:8080` cho thử nghiệm trên chính máy server. Điện thoại cần một URL mà thiết bị truy cập được; localhost trên điện thoại không trỏ tới máy tính. Server hiện chỉ bind 127.0.0.1, cần cấu hình hosting/reverse proxy HTTPS để dùng ngoài máy.

Quét camera trong trang dùng BarcodeDetector trên trình duyệt hỗ trợ, trong secure context. Khi không hỗ trợ hoặc từ chối camera, dùng camera điện thoại mở QR trực tiếp hoặc dán liên kết/mã. Không cần gửi hình camera lên server. Tài liệu API: https://developer.mozilla.org/en-US/docs/Web/API/BarcodeDetector . Tạo QR bằng ZXing core 3.5.3: https://github.com/zxing/zxing .

Kiểm thử tự động bao gồm phân tách quyền member/staff, cô lập lịch sử đơn, vô hiệu hóa phiên khi hồ sơ ngừng hoạt động, JSP tiếng Việt và giải mã lại ảnh SVG QR để xác nhận đúng URL cấu hình. Luồng tạo đơn QR được kiểm tra thêm trên PostgreSQL thật sau triển khai local.

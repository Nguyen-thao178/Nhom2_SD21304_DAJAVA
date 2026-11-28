# Trạng thái công việc F01 đến F08

Bản lưu công việc ngày 28/11/2026. Chưa phải bản hoàn tất để triển khai.

- Đã thêm nền service cho quyền theo chi nhánh, lệnh chống gửi trùng, xác nhận đơn, giữ và tiêu hao nguyên liệu, chế biến, ticket, thu/hoàn tiền, ca và nhật ký.
- Đã thêm các màn hình Vue dùng API thật trong frontend/src/vue cùng client API và session service.
- Các màn hình mới chưa được nối vào router/main hiện tại. Giao diện Vue đang được chạy theo router cũ vẫn là bản mẫu.
- Chưa hoàn tất sắp xếp toàn bộ package và dọn các JSON không dùng.
- Biên dịch trung gian trước đợt sửa gần nhất đã đạt. Chưa kiểm thử tích hợp PostgreSQL, đồng thời hoặc kiểm tra đầy đủ luồng tiền cho bộ service mới.
- Báo cáo SRS trong docs là kết quả rà soát trước khi sửa F01–F08; không dùng nó để xác nhận các service mới đã hoàn thành.

Việc còn lại: nối router và giao diện mới, hoàn thiện các kiểm soát nghiệp vụ, chạy kiểm thử tiền/kho/quyền trên database kiểm thử riêng, build Vue, cập nhật tài liệu theo kết quả thật rồi mới triển khai.

Cấu hình database, mật khẩu tài khoản mẫu, log, target, node_modules và dist không đưa vào Git.
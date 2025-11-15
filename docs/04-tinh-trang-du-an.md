# Báo cáo tình trạng dự án (Doc Formatting Platform - MVP)

Cập nhật trạng thái tính đến 2025-11-15.

## 1. Tóm tắt nhanh
- **Mục tiêu**: triển khai backend Spring Boot phục vụ chuyển đổi tài liệu (sync/async) theo tài liệu kiến trúc.
- **Tiến độ**: khung ứng dụng đã sẵn sàng (API, converter stub, job/file modules), có thể build & chạy với Postgres cục bộ.
- **Độ ổn định**: chạy thông qua `./mvnw clean verify` (dùng H2) + test load cơ bản bằng tay.

## 2. Mức độ hoàn thành theo hạng mục
| Hạng mục | Tình trạng | Ghi chú |
| --- | --- | --- |
| Khởi tạo Spring Boot + cấu trúc domain | ✅ Hoàn thành | Java 21, Spring Boot 3.3.x, Maven wrapper.
| Module `common` (ApiResponse, exception, error code) | ✅ Hoàn thành | Đã có `GlobalExceptionHandler`.
| Module `file` (entity, repo, local storage service) | ✅ Hoàn thành | Lưu file vào thư mục `storage/`.
| Module `converter` + dispatcher + SVG→PNG stub | ✅ Hoàn thành | Sẵn sàng để thêm converter thực tế.
| Module `job` (entity, repo, service CRUD) | ✅ Hoàn thành | Có API tạo job, chưa có worker xử lý thực sự.
| API layer (conversion, job, file) + Swagger | ✅ Hoàn thành | Endpoint chính hoạt động.
| Hạ tầng PostgreSQL/Redis qua Docker Compose | ✅ Hoàn thành | `docker-compose.yml` tạo Postgres 16 + Redis 7.
| Worker async & hàng đợi thực tế | ⚠️ Chưa làm | Cần tích hợp Redis/RabbitMQ + worker riêng.
| Converter thực tế (LibreOffice, PDFBox, ...) | ⚠️ Đang mở | Hiện mới có stub, cần hiện thực hóa khi chọn thư viện.
| Bảo mật (auth, rate-limit) | ⚠️ Chưa làm | MVP chưa bật auth.
| Monitoring nâng cao (tracing, metrics tùy biến) | ⚠️ Chưa làm | Chỉ có Actuator mặc định.

## 3. Chất lượng & kiểm thử
- **Build/Test**: `./mvnw clean verify` dùng profile `test` (H2) → xanh.
- **Manual check**: chạy `./mvnw spring-boot:run -Dspring-boot.run.profiles=dev` + gửi request convert mẫu.
- **Coverage**: chưa đo bằng tool (Jacoco), cần bổ sung khi có logic converter thực tế.

## 4. Rủi ro chính
1. Converter thực tế chưa được tích hợp → khả năng đáp ứng yêu cầu sản phẩm phụ thuộc vào việc chọn thư viện.
2. Job async mới dừng ở CRUD, chưa có worker xử lý → luồng nặng chưa thật sự usable.
3. Chưa có auth/logging nâng cao → cần bổ sung trước khi mở cho môi trường công khai.

## 5. Công việc ưu tiên tiếp theo
1. **Hiện thực hóa converter đầu tiên** (ví dụ Docx→PDF hoặc SVG→PNG bằng thư viện thật).
2. **Viết worker async** sử dụng Redis/RabbitMQ để xử lý job dài.
3. **Bổ sung automation test** cho controller/service chính.
4. **Thiết kế auth cơ bản** (API key/JWT) và logging chuẩn hóa.
5. **Chuẩn bị CI/CD pipeline** (GitHub Actions) để tự động chạy build/test.

## 6. Liên kết hữu ích
- [Tài liệu khởi tạo](./01-khoi-tao-du-an.md)
- [Tài liệu kiến trúc](./02-kien-truc-he-thong.md)
- [Kế hoạch triển khai](./03-ke-hoach-trien-khai.md)
- [Hướng dẫn khởi chạy dự án cho người mới](./05-huong-dan-khoi-chay.md)

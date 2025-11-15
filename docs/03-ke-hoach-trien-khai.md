# Kế hoạch triển khai backend Doc Formatting Platform (MVP)

## 1. Mục tiêu
- Xây dựng nền tảng Spring Boot đáp ứng các yêu cầu MVP đã mô tả trong tài liệu kiến trúc.
- Hỗ trợ tối thiểu một luồng convert đồng bộ (ví dụ SVG → PNG) và khung xử lý job bất đồng bộ.
- Chuẩn hóa tầng API, module domain và cơ chế lưu trữ file nội bộ để dễ mở rộng trong các pha tiếp theo.

## 2. Phạm vi MVP
1. **Core platform**: ứng dụng Spring Boot 3.x, Java 21, Maven.
2. **Module**: `api`, `converter`, `file`, `job`, `common`, `config`.
3. **Hỗ trợ convert sync**: cho phép upload file và trả về file đã qua xử lý (stub logic ở giai đoạn đầu).
4. **Job async**: khởi tạo/lưu job, trả về `jobId`, hỗ trợ endpoint kiểm tra trạng thái.
5. **File storage**: lưu file vào thư mục local theo cấu hình `file-storage.base-dir`.
6. **Observability & actuator**: health check, OpenAPI.
7. **Hạ tầng**: cấu hình PostgreSQL, Docker Compose (Postgres + Redis placeholder), profile `dev`/`prod`.

## 3. Lộ trình thực hiện
| Giai đoạn | Hạng mục | Đầu ra |
| --- | --- | --- |
| G1 | Khởi tạo project Spring Boot, cấu trúc thư mục domain | Skeleton mã nguồn, cấu hình Maven |
| G2 | Xây dựng module common (DTO response, exception, error code) + config | Response chuẩn, Exception handler, cấu hình properties |
| G3 | Implement module file (entity, repository, service lưu file local) | API lưu/đọc file nội bộ |
| G4 | Implement module converter cơ bản (interface, dispatcher, 1 converter stub) | Luồng convert sync hoàn chỉnh |
| G5 | Implement module job (entity, repository, service CRUD, scheduler stub) | API quản lý job async |
| G6 | API layer (ConversionController, JobController, FileController) + OpenAPI | Endpoint REST hoạt động |
| G7 | Thiết lập Docker Compose + hướng dẫn run/test | Dev env đầy đủ |

## 4. Ưu tiên kỹ thuật
- **Domain-first packaging**: đặt class theo domain module.
- **Record DTO**: ưu tiên Java record để giảm boilerplate.
- **Configuration binding**: dùng `@ConfigurationProperties` cho `file-storage`.
- **Validation**: tích hợp `jakarta.validation` với `@Validated` service/controller.
- **Transaction**: đánh dấu service quan trọng với `@Transactional` khi thao tác DB.

## 5. Backlog chi tiết
1. Tạo entity `StoredFile`, `Job` + repository JPA.
2. Viết service lưu file (`LocalFileStorageService`) đọc config `baseDir`.
3. Thiết kế `ConversionType`, `ConversionRequest`, `ConversionResult`, `Converter` interface.
4. Tạo `ConversionDispatcher` + converter mẫu (`SvgToPngStubConverter`) để mô phỏng xử lý.
5. API `POST /api/v1/convert/{type}`: nhận file, options JSON, trả về metadata file kết quả.
6. API async `POST /api/v1/jobs/{type}` + `GET /api/v1/jobs/{id}` + `GET /api/v1/jobs/{id}/result`.
7. API file `GET /api/v1/files/{id}` để download.
8. Viết `docker-compose.yml` cho Postgres + Redis.
9. Viết hướng dẫn run (`README` update).

## 6. Tiêu chí hoàn thành
- Build `mvn clean install` thành công.
- Endpoint health (`/actuator/health`) trả UP.
- Endpoint convert trả về cấu trúc `{success,data,error}`.
- Job tạo được trong DB (PENDING) và có thể cập nhật thủ công (stub worker).
- File được lưu vào thư mục `storage/` trong repo.

## 7. Bước tiếp theo sau MVP
- Bổ sung worker bất đồng bộ thực sự (sử dụng Redis/RabbitMQ).
- Tích hợp các thư viện chuyển đổi thực tế (LibreOffice, Apache POI, ImageMagick...).
- Di chuyển storage sang S3 và thêm module auth.
- Bổ sung metrics/tracing nâng cao.

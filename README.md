# Doc Formatting Platform (MVP)

Backend Spring Boot triển khai theo tài liệu kiến trúc trong thư mục `docs/`.

## Yêu cầu hệ thống
- Java 21
- Maven 3.9+
- Docker (nếu chạy Postgres/Redis qua compose)

## Cấu trúc chính
- `docs/` – tài liệu khởi tạo, kiến trúc, kế hoạch triển khai.
- `src/main/java/com/example/docformatting/` – mã nguồn backend theo module domain (`api`, `converter`, `file`, `job`, `common`).
- `storage/` – thư mục lưu file local (được cấu hình qua `file-storage.base-dir`).

> **Tài liệu nên xem đầu tiên**
> - [Báo cáo tình trạng dự án](docs/04-tinh-trang-du-an.md)
> - [Hướng dẫn khởi chạy cho người mới](docs/05-huong-dan-khoi-chay.md)

## Chạy dịch vụ hỗ trợ
```bash
docker compose up -d
```
File `docker-compose.yml` tạo Postgres 16 và Redis 7 phục vụ môi trường dev. Chi tiết từng bước xem thêm trong `docs/05-huong-dan-khoi-chay.md`.

## Build & run
```bash
./mvnw clean install
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

Swagger/OpenAPI có sẵn tại `http://localhost:8080/swagger-ui.html`.

## Endpoint chính
- `POST /api/v1/convert/{type}` – convert đồng bộ (ví dụ `svg_to_png`).
- `POST /api/v1/jobs/{type}` – tạo job bất đồng bộ.
- `GET /api/v1/jobs/{id}` – theo dõi trạng thái job.
- `GET /api/v1/jobs/{id}/result` – tải kết quả khi job hoàn thành.
- `GET /api/v1/files/{fileId}` – tải file bất kỳ.

Tất cả response đều bọc trong `ApiResponse { success, data, error }`.

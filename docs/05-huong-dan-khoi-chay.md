# Hướng dẫn khởi chạy dự án cho người mới

Tài liệu này giúp thành viên mới có thể setup và chạy Doc Formatting Platform (MVP) trong vòng vài phút.

## 1. Kiến thức & công cụ yêu cầu
- Java 21 (Temurin/Oracle đều được).
- Maven 3.9+ (đã kèm `mvnw`, có thể không cần cài riêng nếu dùng Linux/macOS).
- Docker + Docker Compose V2.
- Git.

> **Tip**: Kiểm tra nhanh phiên bản bằng `java -version`, `./mvnw -v`, `docker --version`.

## 2. Clone & chuẩn bị môi trường
```bash
# Clone repo
 git clone https://github.com/<org>/conversion-plf.git
 cd conversion-plf

# Tạo file cấu hình cá nhân (nếu cần override)
 cp src/main/resources/application-dev.yml src/main/resources/application-local.yml
```

Thư mục quan trọng:
- `storage/`: nơi lưu file đã upload/convert (được tạo sẵn). Nếu chạy trên Linux/macOS, bảo đảm user hiện tại có quyền đọc/ghi.
- `.env` (tùy chọn): có thể tạo để chứa biến môi trường như `SPRING_DATASOURCE_URL` khi triển khai thật.

## 3. Khởi động dịch vụ phụ trợ
Dự án dùng PostgreSQL và Redis thông qua Docker Compose cho môi trường dev.

```bash
docker compose up -d
```

- PostgreSQL: `jdbc:postgresql://localhost:5432/doc_formatting`, user/pass `doc_user/doc_pass` (định nghĩa trong `docker-compose.yml`).
- Redis hiện chỉ để dành cho tương lai; container vẫn khởi chạy để bảo đảm compatibility.

Để dừng và xóa container: `docker compose down -v`.

## 4. Cấu hình ứng dụng
Ứng dụng có các profile:
- `dev` (mặc định khi chạy cục bộ) – dùng PostgreSQL thật.
- `test` – dùng H2 in-memory khi chạy `./mvnw test` hoặc `./mvnw clean verify`.
- `prod` – placeholder cho môi trường triển khai, chỉnh sửa trong `src/main/resources/application-prod.yml`.

Nếu cần override thông số (ví dụ đường dẫn lưu file), có thể:
```bash
export FILE_STORAGE_BASE_DIR=/tmp/doc-storage
export SPRING_PROFILES_ACTIVE=dev
```
hoặc chỉnh trực tiếp trong `application-dev.yml`.

## 5. Build & chạy ứng dụng
```bash
# Build + chạy toàn bộ test
./mvnw clean verify

# Khởi chạy API với profile dev
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

Ứng dụng chạy tại `http://localhost:8080`.
- Health check: `http://localhost:8080/actuator/health`
- Swagger UI: `http://localhost:8080/swagger-ui.html`

## 6. Kiểm tra nhanh bằng cURL
### 6.1 Convert đồng bộ (SVG → PNG stub)
```bash
curl -X POST "http://localhost:8080/api/v1/convert/svg_to_png" \
  -F "file=@samples/input.svg" \
  -F 'options={"width":200}' \
  -H "accept: application/json"
```

Kết quả trả về `fileId` + `downloadUrl`. Tải file bằng:
```bash
curl -L "http://localhost:8080/api/v1/files/{fileId}" -o output.png
```

### 6.2 Job bất đồng bộ
```bash
curl -X POST "http://localhost:8080/api/v1/jobs/svg_to_png" \
  -F "file=@samples/input.svg" \
  -F 'options={"width":200}'

curl "http://localhost:8080/api/v1/jobs/{jobId}"
```

Hiện worker chưa xử lý thật, job sẽ ở trạng thái `PENDING`. Đây là baseline để phát triển worker riêng.

## 7. Troubleshooting
| Vấn đề | Cách xử lý |
| --- | --- |
| `docker compose up -d` báo port 5432 bận | Dừng Postgres cục bộ khác hoặc chỉnh `ports` trong `docker-compose.yml`. |
| `./mvnw spring-boot:run` lỗi connect DB | Kiểm tra container Postgres đã chạy, hoặc chỉnh `spring.datasource.*` trỏ tới DB đúng. |
| Không ghi được file vào `storage/` | Đảm bảo thư mục tồn tại và user có quyền ghi (`chmod -R 775 storage`). |
| Swagger không lên | Kiểm tra log để chắc server đã khởi động thành công, xem `localhost:8080/swagger-ui.html`. |

## 8. Liên hệ & tài liệu bổ sung
- [Báo cáo tình trạng dự án](./04-tinh-trang-du-an.md)
- [Kế hoạch triển khai](./03-ke-hoach-trien-khai.md)
- [Tài liệu kiến trúc](./02-kien-truc-he-thong.md)

Có câu hỏi hãy ping lead backend hoặc tạo issue trên repo.

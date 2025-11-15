# DOC File Formatting Platform – Khởi tạo dự án (Java Spring Boot)

## 1. Mục tiêu

Xây dựng một service backend bằng **Java + Spring Boot** phục vụ các tính năng:

- Gen PDF từ file template (docx/html) + dataset.
- PDF → HTML.
- SVG → PNG.
- Nén ảnh (compress images).
- Gen file Excel từ template + dataset.
- Cung cấp REST API để hệ thống khác tích hợp.

Kiến trúc ban đầu: **modular monolith**, dễ mở rộng thành microservices sau này.

---

## 2. Tech stack & tiêu chuẩn

### 2.1. Ngôn ngữ & runtime

- Java 21 (LTS) hoặc 17 (nếu hạ thấp yêu cầu).
- Spring Boot 3.x (Spring Framework 6).

### 2.2. Build & quản lý dependency

- **Maven** (mặc định)  
  - GroupId: `com.example`
  - ArtifactId: `doc-formatting-platform`
  - Packaging: `jar`

*(Bạn có thể dùng Gradle nếu team quen, nhưng tài liệu này sẽ dùng Maven.)*

### 2.3. Hệ sinh thái chính

- Spring Boot Starter:
  - `spring-boot-starter-web` – REST API.
  - `spring-boot-starter-validation` – validate request.
  - `spring-boot-starter-actuator` – health, metrics.
  - `spring-boot-starter-test` – test.
- API docs:
  - `springdoc-openapi-starter-webmvc-ui` – Swagger UI.
- Persistence (nếu lưu metadata job/file):
  - `spring-boot-starter-data-jpa` + PostgreSQL driver.
- Queue (tùy chọn – nếu cần xử lý async):
  - Redis (Spring Data Redis) hoặc RabbitMQ.

### 2.4. Thư viện xử lý file (dự kiến)

(Chỉ là gợi ý để team sau này chọn chi tiết)

- DOCX/Template:
  - `docx4j` hoặc Apache POI.
- PDF:
  - Apache PDFBox, iText (chú ý license), hoặc call ra LibreOffice (JODConverter).
- Excel:
  - Apache POI.
- Ảnh:
  - Wrapper ra ImageMagick, GraphicsMagick hoặc dùng Java image libs (`thumbnailator`, `imgscalr`).
- SVG:
  - Apache Batik hoặc wrapper tool như `rsvg-convert` thông qua process builder.

---

## 3. Chuẩn bị môi trường

### 3.1. Cài đặt bắt buộc

- Java JDK 21 (hoặc 17).
- Maven 3.8+.
- Git.
- Docker & Docker Compose (nếu muốn chạy PostgreSQL/Redis local).
- IDE: IntelliJ IDEA / VS Code / Eclipse (khuyến nghị IntelliJ).

### 3.2. Kiểm tra phiên bản

```bash
java -version
mvn -v
git --version
docker -v

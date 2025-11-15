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
```

---

## 4. Tạo project Spring Boot

### 4.1. Sử dụng Spring Initializr (web)

1. Truy cập `https://start.spring.io` và chọn:
   - Project: **Maven**
   - Language: **Java**
   - Spring Boot: **3.x**
2. Project Metadata:
   - Group: `com.example`
   - Artifact: `doc-formatting-platform`
   - Name: `doc-formatting-platform`
   - Package name: `com.example.docformatting`
   - Packaging: `Jar`
   - Java: `21`
3. Dependencies:
   - Spring Web
   - Validation
   - Spring Data JPA
   - PostgreSQL Driver
   - Spring Boot Actuator
   - Springdoc OpenAPI UI (hoặc thêm sau)
   - (Tuỳ chọn) Spring Data Redis / AMQP
4. Tải project `.zip` về và giải nén.

### 4.2. Tạo project bằng curl (CLI – optional)

```bash
curl https://start.spring.io/starter.zip \
  -d dependencies=web,validation,actuator,data-jpa,postgresql \
  -d groupId=com.example \
  -d artifactId=doc-formatting-platform \
  -d name=doc-formatting-platform \
  -d packageName=com.example.docformatting \
  -d javaVersion=21 \
  -o doc-formatting-platform.zip

unzip doc-formatting-platform.zip
cd doc-formatting-platform
```

---

## 5. Cấu trúc thư mục đề xuất

Sau khi khởi tạo, chuẩn hoá lại structure theo module domain:

```
doc-formatting-platform/
├─ docs/
│  ├─ 01-khoi-tao-du-an.md
│  └─ 02-kien-truc-he-thong.md
├─ src/
│  ├─ main/
│  │  ├─ java/com/example/docformatting/
│  │  │  ├─ DocFormattingPlatformApplication.java
│  │  │  ├─ config/
│  │  │  ├─ common/          # exception, util, constants
│  │  │  ├─ job/             # quản lý job, queue
│  │  │  ├─ file/            # upload, storage, metadata
│  │  │  ├─ converter/       # core logic convert nén/đổi định dạng
│  │  │  │  ├─ doc/          # doc/docx ↔ pdf
│  │  │  │  ├─ pdf/
│  │  │  │  ├─ image/
│  │  │  │  └─ excel/
│  │  │  └─ api/             # controller REST
│  │  └─ resources/
│  │     ├─ application.yml
│  │     ├─ application-dev.yml
│  │     └─ application-prod.yml
│  └─ test/java/com/example/docformatting/
│     └─ ...
├─ pom.xml
└─ README.md
```

---

## 6. Cấu hình ứng dụng

### 6.1. `src/main/resources/application.yml`

```yaml
spring:
  application:
    name: doc-formatting-platform

  datasource:
    url: jdbc:postgresql://localhost:5432/doc_formatting
    username: doc_user
    password: doc_pass
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true

server:
  port: 8080

file-storage:
  base-dir: ./storage   # thư mục lưu file local

job:
  async-enabled: true
```

### 6.2. Profiles

- `application-dev.yml` – cấu hình cho dev (DB local, log debug, cors mở).
- `application-prod.yml` – cấu hình production (DB cloud, S3, logging…).

Ví dụ chạy:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

---

## 7. Chạy các service phụ trợ (tuỳ chọn)

### 7.1. PostgreSQL + Redis bằng Docker Compose

Tạo file `docker-compose.yml` ở root:

```yaml
version: "3.8"

services:
  postgres:
    image: postgres:16
    container_name: doc-postgres
    environment:
      POSTGRES_DB: doc_formatting
      POSTGRES_USER: doc_user
      POSTGRES_PASSWORD: doc_pass
    ports:
      - "5432:5432"
    volumes:
      - pgdata:/var/lib/postgresql/data

  redis:
    image: redis:7
    container_name: doc-redis
    ports:
      - "6379:6379"

volumes:
  pgdata:
```

Chạy:

```bash
docker compose up -d
```

---

## 8. Kiểm tra chạy ứng dụng

```bash
mvn clean install
mvn spring-boot:run
```

Truy cập:

- `http://localhost:8080/actuator/health` – health check.
- `http://localhost:8080/swagger-ui.html` – Swagger UI (sau khi thêm springdoc).

---

## 9. Quy ước coding cơ bản

- Package theo domain (`converter`, `job`, `file`…) thay vì chỉ theo layer.
- Tên class:
  - Controller: `XxxController`
  - Service: `XxxService`
  - Repository: `XxxRepository`
- Sử dụng `@RestController + @RequestMapping("/api/v1/...")`.
- Response chuẩn: bọc trong DTO `{ "success": true/false, "data": ..., "error": ... }`.
- Exception handling:
  - Tạo `@ControllerAdvice + @ExceptionHandler` để map lỗi → HTTP status.

### 9.1. Converter interface ví dụ

```java
public interface Converter {
    boolean supports(ConversionType type);

    ConversionResult convert(ConversionRequest request) throws ConversionException;
}
```

### 9.2. ConversionDispatcher

- Được inject list các `Converter`.
- Chọn converter phù hợp dựa trên `ConversionType` và delegate xử lý.

### 9.3. Module `common`

- Exception / error code / util:
  - `BusinessException`, `NotFoundException`, `ValidationException`
  - `GlobalExceptionHandler` (`@ControllerAdvice`)
  - `ErrorCode` enum

---

## 10. Luồng xử lý chính

### 10.1. Luồng xử lý đồng bộ (ví dụ: SVG → PNG nhỏ)

1. Client gọi `POST /api/v1/convert/svg-to-png` kèm file & options.
2. `ConversionController` validate request.
3. Lưu file input bằng `FileService` (nếu cần).
4. Tạo `ConversionRequest` và gọi `ConversionDispatcher`.
5. `ConversionDispatcher` chọn `SvgToPngConverter`:
   - Đọc file.
   - Convert sang PNG.
   - Lưu file kết quả qua `FileService`.
6. Trả response chứa `fileId` hoặc URL download trực tiếp.

### 10.2. Luồng xử lý bất đồng bộ (ví dụ: docx template → PDF nặng)

1. Client gọi `POST /api/v1/jobs/doc-template-to-pdf` kèm file template, JSON data, `async=true`.
2. `JobController` lưu file template, tạo Job (status = `PENDING`), push vào queue hoặc đánh dấu chờ worker.
3. Trả về `jobId`.
4. Worker (`JobProcessor`) đọc job pending, build `ConversionRequest`, gọi `ConversionDispatcher` → `DocTemplateToPdfConverter`.
5. Lưu file output, update job status = `DONE` + set `resultFileId`.
6. Nếu lỗi, update status = `FAILED` + `errorMessage`.
7. Client poll `GET /api/v1/jobs/{jobId}` để nhận trạng thái + link file khi DONE.

---

## 11. Roadmap kiến trúc

- **Phase 1**: Monolith với module `api`, `job`, `file`, `converter`. Storage local, xử lý sync + async basic (`DB job + @Scheduled`).
- **Phase 2**: Tách worker xử lý job nặng. Thêm Redis/RabbitMQ cho queue. Tích hợp S3.
- **Phase 3**: Khi tải lớn → tách thành microservice (`conversion-service`, `job-service`, `file-service`), thêm API Gateway + centralized auth.

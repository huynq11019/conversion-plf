# DOC File Formatting Platform – Kiến trúc hệ thống

## 1. Mục tiêu & phạm vi

Hệ thống backend cung cấp API để:

1. Gen PDF từ file template (docx/html) + dataset (JSON).
2. Chuyển đổi định dạng:
   - PDF → HTML
   - SVG → PNG
   - Ảnh (jpg/png) → ảnh nén (jpg/webp/...)
3. Gen file Excel từ template + dataset.

Yêu cầu bổ sung:

- Hỗ trợ xử lý **đồng bộ** cho file nhỏ/đơn giản.
- Hỗ trợ xử lý **bất đồng bộ** (job queue) cho file lớn hoặc batch.
- Dễ dàng mở rộng để thêm converter mới.

---

## 2. Kiến trúc tổng thể

### 2.1. Kiểu kiến trúc

- Giai đoạn đầu: **Modular Monolith (Spring Boot)**.
- Ứng dụng stateless, file & metadata lưu ngoài:
  - **Database**: PostgreSQL – lưu job, metadata file.
  - **Object Storage**: local storage (dev) / S3-compatible (prod).
  - **Queue** (tùy chọn): Redis/RabbitMQ – dispatch job.

### 2.2. Sơ đồ logic (mô tả text)

```
Client (Web/App/Service khác)
→ REST API (Spring Boot)
→ Job & File Service
→ Converter Core (Doc/PDF/Image/Excel)
→ Storage (file) + Database (metadata/job)
→ Trả về URL / jobId cho client
```

Khi xử lý async:

```
Client → tạo Job (status = PENDING)
→ Worker đọc job từ DB/Queue, xử lý
→ Update status = DONE/FAILED (+ resultFileId)
→ Client poll GET /jobs/{id} hoặc nhận webhook
```

---

## 3. Phân tách module (trong monolith)

### 3.1. Module `api`

- **Nhiệm vụ**:
  - Expose REST endpoints.
  - Mapping request/response DTO.
  - Không chứa business logic phức tạp.
- **Thành phần chính**:
  - `api.controller` – `ConversionController`, `TemplateController`, `JobController`, `FileController` (nếu cần).
  - `api.dto` – `CreateConversionRequest`, `CreateConversionResponse`, `JobStatusResponse`, `ErrorResponse`…

### 3.2. Module `job`

- **Nhiệm vụ**:
  - Quản lý job async: tạo, cập nhật trạng thái, truy vấn.
  - Tích hợp với queue nếu cần.
- **Thành phần**:
  - `job.entity.Job` với các field: `id`, `type`, `status`, `inputFileId`, `resultFileId`, `optionsJson`, `errorMessage`, `createdAt`, `updatedAt`.
  - `job.enums.JobType`, `JobStatus`.
  - `job.repository.JobRepository`.
  - `job.service.JobService`.
  - `job.worker.JobProcessor` (dùng `@Scheduled`, `@Async` hoặc service riêng).

### 3.3. Module `file`

- **Nhiệm vụ**:
  - Upload/download file.
  - Quản lý metadata file (tên, size, mime type, path).
  - Abstraction cho storage (local/S3/...).
- **Thành phần**:
  - `file.entity.StoredFile`.
  - `file.repository.StoredFileRepository`.
  - `file.storage.FileStorage` (interface) + implementation: `LocalFileStorage`, `S3FileStorage`.
  - `file.service.FileService` (upload, download, delete, tạo signed URL...).

### 3.4. Module `converter`

- **Nhiệm vụ**: chứa toàn bộ logic chuyển đổi/nén, thiết kế plug-in style.
- **Cấu trúc**:

```
converter/
├─ core/
│  ├─ ConversionType (enum)
│  ├─ ConversionRequest (input)
│  ├─ ConversionResult (output)
│  └─ Converter (interface)
├─ doc/
│  └─ DocTemplateToPdfConverter
├─ pdf/
│  └─ PdfToHtmlConverter
├─ image/
│  ├─ SvgToPngConverter
│  └─ ImageCompressConverter
├─ excel/
│  └─ ExcelTemplateGenerator
└─ service/
   └─ ConversionDispatcher
```

### 3.5. Module `common`

- Exception / error code / util chung toàn hệ thống.
- Thành phần: `BusinessException`, `NotFoundException`, `ValidationException`, `ErrorCode` enum, `GlobalExceptionHandler`, helper log/tracing, constants.

---

## 4. Luồng xử lý chính

### 4.1. Luồng đồng bộ

1. Client gọi `POST /api/v1/convert/{conversionType}` với multipart form (`file`, `data`, `options`).
2. Controller validate, lưu input bằng `FileService` (nếu cần), tạo `ConversionRequest`.
3. `ConversionDispatcher` chọn converter phù hợp dựa trên `ConversionType` → xử lý và lưu output.
4. Trả response `{ success: true, data: { fileId, downloadUrl } }`.

### 4.2. Luồng bất đồng bộ

1. Client gửi `POST /api/v1/jobs/{conversionType}` (`async=true`).
2. `JobController` lưu file, tạo record Job (`status=PENDING`, `inputFileId`).
3. Job được push vào queue hoặc worker đọc bằng `@Scheduled`.
4. Worker xây `ConversionRequest`, gọi `ConversionDispatcher`.
5. Lưu output, cập nhật job (`status=DONE`, `resultFileId`). Nếu lỗi → `FAILED + errorMessage`.
6. Client poll `GET /api/v1/jobs/{jobId}` hoặc `GET /api/v1/jobs/{jobId}/result` để tải file.

---

## 5. Thiết kế API (high-level)

### 5.1. Endpoint nhóm conversion (sync)

- `POST /api/v1/convert/doc-template-to-pdf`
- `POST /api/v1/convert/pdf-to-html`
- `POST /api/v1/convert/svg-to-png`
- `POST /api/v1/convert/image-compress`
- `POST /api/v1/convert/excel-template`

Request: `multipart/form-data` gồm `file` (bắt buộc), `data` (JSON string, optional), `options` (JSON string, optional).

Response:

```json
{
  "success": true,
  "data": {
    "fileId": "123",
    "downloadUrl": "https://..."
  },
  "error": null
}
```

### 5.2. Endpoint nhóm jobs (async)

- `POST /api/v1/jobs/{conversionType}` – tạo job.
- `GET /api/v1/jobs/{jobId}` – lấy trạng thái job.
- `GET /api/v1/jobs/{jobId}/result` – tải file kết quả.

### 5.3. Endpoint files (nếu cần)

- `GET /api/v1/files/{fileId}` – download.
- `DELETE /api/v1/files/{fileId}` – xoá file.

---

## 6. Thiết kế dữ liệu

### 6.1. Bảng `jobs`

| Column         | Type      | Ghi chú                                      |
| -------------- | --------- | -------------------------------------------- |
| id             | UUID      | primary key                                  |
| type           | varchar   | `DOC_TEMPLATE_TO_PDF`, ...                   |
| status         | varchar   | `PENDING`/`RUNNING`/`DONE`/`FAILED`          |
| input_file_id  | UUID      | FK → `stored_files`                          |
| result_file_id | UUID      | FK → `stored_files` (nullable)               |
| options_json   | text      | lưu options (JSON)                           |
| error_message  | text      | nullable                                     |
| created_at     | timestamp |                                              |
| updated_at     | timestamp |                                              |

### 6.2. Bảng `stored_files`

| Column        | Type      | Ghi chú                               |
| ------------- | --------- | ------------------------------------- |
| id            | UUID      | primary key                           |
| file_name     | varchar   | tên file gốc                          |
| storage_path  | varchar   | path / key trong S3                   |
| size_in_bytes | bigint    | dung lượng                            |
| mime_type     | varchar   |                                       |
| created_at    | timestamp |                                       |

---

## 7. Bảo mật & tính toàn vẹn

- Auth: JWT (Bearer token) hoặc API key cho internal service.
- Upload file:
  - Giới hạn size (`spring.servlet.multipart.max-file-size`, `max-request-size`).
  - Kiểm tra mime type/extension.
  - Không thực thi file upload (chỉ lưu raw data).
- Converter nên chạy sandbox/container riêng nếu gọi external tools (LibreOffice, ImageMagick...).

---

## 8. Khả năng mở rộng & triển khai

### 8.1. Scaling

- App Spring Boot stateless → scale horizontal (nhiều instance).
- File & DB:
  - Migration sang S3/Cloud storage + managed DB khi lên prod.
- Queue:
  - Tách Job Worker thành service riêng nếu tải cao.

### 8.2. Deployment

- Docker hoá app (multi-stage build: Maven build → runtime image).
- Dev: Docker Compose (App + Postgres + Redis).
- Staging/Prod: Kubernetes hoặc dịch vụ container (ECS, GKE,…).

---

## 9. Observability

- Logging: dùng slf4j + logback, log theo `requestId`/`jobId` để trace.
- Metrics: Spring Actuator (`/actuator/health`, `/actuator/metrics`), theo dõi số job, thời gian xử lý, lỗi.
- Tracing (optional): OpenTelemetry + exporter (Jaeger/Zipkin).

---

## 10. Hướng dẫn mở rộng: thêm converter mới

Ví dụ thêm **PDF → Text**:

1. Update `ConversionType` enum: `PDF_TO_TEXT`.
2. Tạo `PdfToTextConverter implements Converter`:

```java
@Component
public class PdfToTextConverter implements Converter {
    @Override
    public boolean supports(ConversionType type) {
        return ConversionType.PDF_TO_TEXT.equals(type);
    }

    @Override
    public ConversionResult convert(ConversionRequest request) {
        // 1. Lấy file input
        // 2. Parse PDF → text (Apache PDFBox)
        // 3. Lưu kết quả (file .txt hoặc JSON)
        // 4. Trả ConversionResult
    }
}
```

3. Đảm bảo `ConversionController` mapping conversionType mới, viết test cho converter & endpoint.

---

## 11. Roadmap kiến trúc

- **Phase 1**: Implement monolith với module: `api`, `job`, `file`, `converter`. Storage local, xử lý sync + async basic (DB job + `@Scheduled`).
- **Phase 2**: Tách worker xử lý job nặng. Thêm Redis/RabbitMQ cho queue. Tích hợp S3.
- **Phase 3**: Nếu tải lớn: tách thành microservice (`conversion-service`, `job-service`, `file-service`), thêm API Gateway, centralized auth.

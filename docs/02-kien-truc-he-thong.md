
---

## `docs/02-kien-truc-he-thong.md` – Tài liệu kiến trúc

```markdown
# DOC File Formatting Platform – Kiến trúc hệ thống

## 1. Mục tiêu & phạm vi

Hệ thống backend cung cấp API để:

1. Gen PDF từ file template (docx/html) + dataset (JSON).
2. Chuyển đổi:
   - PDF → HTML
   - SVG → PNG
   - Ảnh (jpg/png) → ảnh nén (jpg/webp/...)
3. Gen file Excel từ template + dataset.

Hỗ trợ:

- Xử lý đồng bộ cho file nhỏ/đơn giản.
- Xử lý bất đồng bộ (job queue) cho file lớn hoặc batch.
- Dễ mở rộng để thêm converter mới.

---

## 2. Kiến trúc tổng thể

### 2.1. Kiểu kiến trúc

- Giai đoạn đầu: **Modular Monolith (Spring Boot)**.
- Ứng dụng stateless, file & metadata lưu ngoài:
  - **Database**: PostgreSQL – lưu job, metadata file.
  - **Object Storage**: local storage (dev) / S3-compatible (prod).
  - **Queue** (tùy chọn): Redis/RabbitMQ – dispatch job.

### 2.2. Sơ đồ logic (mô tả text)

Client (Web/App/Service khác)  
→ **REST API (Spring Boot)**  
→ **Job & File Service**  
→ **Converter Core** (Doc/PDF/Image/Excel)  
→ Ghi file ra Storage & metadata ra DB  
→ Trả về URL / jobId cho client.

Khi xử lý async:

Client  
→ Tạo job (status = PENDING)  
→ Worker (Spring @Async, scheduler hoặc process riêng) đọc job từ DB/Queue, xử lý  
→ Cập nhật status = DONE/FAILED  
→ Client poll `/jobs/{id}` hoặc nhận webhook (nếu implement).

---

## 3. Phân tách module (trong monolith)

### 3.1. Module `api`

- **Nhiệm vụ**:
  - Expose REST endpoints.
  - Mapping request/response DTO.
  - Không chứa business logic phức tạp.

- Thành phần:
  - `api.controller`:
    - `ConversionController`
    - `TemplateController`
    - `JobController`
  - `api.dto`:
    - `CreateConversionRequest`, `CreateConversionResponse`
    - `JobStatusResponse`
    - `ErrorResponse`

### 3.2. Module `job`

- **Nhiệm vụ**:
  - Quản lý job async:
    - tạo job
    - cập nhật trạng thái
    - truy vấn job
  - Tích hợp với queue nếu cần.

- Thành phần:
  - `job.entity.Job`
    - `id`, `type`, `status`, `inputFileId`, `resultFileId`, `createdAt`, `updatedAt`, `errorMessage`, ...
  - `job.enums.JobType`, `JobStatus`
  - `job.repository.JobRepository`
  - `job.service.JobService`
  - `job.worker.JobProcessor` (nếu dùng @Scheduled/@Async)

### 3.3. Module `file`

- **Nhiệm vụ**:
  - Upload/download file.
  - Quản lý metadata file (tên, size, mime type, path).
  - Abstraction cho storage (local/S3/...).

- Thành phần:
  - `file.entity.StoredFile`
  - `file.repository.StoredFileRepository`
  - `file.storage.FileStorage` (interface)
  - `file.storage.LocalFileStorage`, `file.storage.S3FileStorage`
  - `file.service.FileService`

### 3.4. Module `converter`

- **Nhiệm vụ**:
  - Chứa toàn bộ logic chuyển đổi/nén.
  - Được thiết kế plug-in style: mỗi converter implement 1 interface chung.

- Thành phần:

```text
converter/
├─ core/
│  ├─ ConversionType (enum)
│  ├─ ConversionRequest (input)
│  ├─ ConversionResult (output)
│  └─ Converter (interface)
├─ doc/
│  ├─ DocTemplateToPdfConverter
├─ pdf/
│  ├─ PdfToHtmlConverter
├─ image/
│  ├─ SvgToPngConverter
│  ├─ ImageCompressConverter
├─ excel/
│  ├─ ExcelTemplateGenerator
└─ service/
   ├─ ConversionDispatcher

package com.example.docformatting.api.controller;

import com.example.docformatting.common.dto.ApiResponse;
import com.example.docformatting.common.exception.NotFoundException;
import com.example.docformatting.common.exception.ValidationException;
import com.example.docformatting.converter.ConversionType;
import com.example.docformatting.file.entity.StoredFile;
import com.example.docformatting.file.service.FileStorageService;
import com.example.docformatting.job.dto.JobResponse;
import com.example.docformatting.job.service.JobService;
import jakarta.validation.constraints.NotNull;
import java.io.IOException;
import java.util.UUID;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/jobs")
public class JobController {

    private final JobService jobService;
    private final FileStorageService fileStorageService;

    public JobController(JobService jobService, FileStorageService fileStorageService) {
        this.jobService = jobService;
        this.fileStorageService = fileStorageService;
    }

    @PostMapping(value = "/{type}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<JobResponse> createJob(@PathVariable("type") ConversionType type,
                                              @RequestPart("file") @NotNull MultipartFile file,
                                              @RequestPart(value = "options", required = false) String options) throws IOException {
        if (file.isEmpty()) {
            throw new ValidationException("File must not be empty");
        }
        StoredFile storedFile = fileStorageService.save(file);
        JobResponse response = jobService.createPendingJob(type, storedFile, options);
        return ApiResponse.success(response);
    }

    @GetMapping("/{jobId}")
    public ApiResponse<JobResponse> getJob(@PathVariable UUID jobId) {
        return ApiResponse.success(jobService.getJob(jobId));
    }

    @GetMapping("/{jobId}/result")
    public ResponseEntity<Resource> downloadResult(@PathVariable UUID jobId) {
        StoredFile storedFile = jobService.findResult(jobId)
                .orElseThrow(() -> new NotFoundException("Job result not ready"));
        Resource resource = fileStorageService.loadAsResource(storedFile.getId());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(storedFile.getMimeType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + storedFile.getFileName() + "\"")
                .body(resource);
    }
}

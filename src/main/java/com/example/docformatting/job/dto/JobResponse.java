package com.example.docformatting.job.dto;

import com.example.docformatting.converter.ConversionType;
import com.example.docformatting.job.entity.JobStatus;
import java.time.Instant;
import java.util.UUID;

public record JobResponse(UUID jobId, ConversionType type, JobStatus status, UUID inputFileId,
                          UUID resultFileId, String errorMessage, Instant updatedAt) {
}

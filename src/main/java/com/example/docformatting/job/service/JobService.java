package com.example.docformatting.job.service;

import com.example.docformatting.common.exception.NotFoundException;
import com.example.docformatting.converter.ConversionType;
import com.example.docformatting.file.entity.StoredFile;
import com.example.docformatting.job.dto.JobResponse;
import com.example.docformatting.job.entity.Job;
import com.example.docformatting.job.entity.JobStatus;
import com.example.docformatting.job.repository.JobRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class JobService {

    private final JobRepository jobRepository;

    public JobService(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    @Transactional
    public JobResponse createPendingJob(ConversionType type, StoredFile inputFile, String optionsJson) {
        Job job = new Job();
        job.setType(type);
        job.setStatus(JobStatus.PENDING);
        job.setInputFile(inputFile);
        job.setOptionsJson(optionsJson);
        Job saved = jobRepository.save(job);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public JobResponse getJob(UUID jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new NotFoundException("Job %s not found".formatted(jobId)));
        return toResponse(job);
    }

    @Transactional(readOnly = true)
    public Optional<StoredFile> findResult(UUID jobId) {
        return jobRepository.findById(jobId)
                .map(Job::getResultFile);
    }

    private JobResponse toResponse(Job job) {
        UUID inputId = job.getInputFile() != null ? job.getInputFile().getId() : null;
        UUID resultId = job.getResultFile() != null ? job.getResultFile().getId() : null;
        return new JobResponse(job.getId(), job.getType(), job.getStatus(), inputId, resultId, job.getErrorMessage(), job.getUpdatedAt());
    }
}

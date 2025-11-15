package com.example.docformatting.job.repository;

import com.example.docformatting.job.entity.Job;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobRepository extends JpaRepository<Job, UUID> {
}

package com.example.docformatting.file.service;

import com.example.docformatting.common.exception.NotFoundException;
import com.example.docformatting.config.FileStorageProperties;
import com.example.docformatting.file.entity.StoredFile;
import com.example.docformatting.file.repository.StoredFileRepository;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.UUID;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class LocalFileStorageService implements FileStorageService {

    private final StoredFileRepository storedFileRepository;
    private final FileStorageProperties properties;

    public LocalFileStorageService(StoredFileRepository storedFileRepository, FileStorageProperties properties) {
        this.storedFileRepository = storedFileRepository;
        this.properties = properties;
        initializeBaseDir();
    }

    private void initializeBaseDir() {
        try {
            Files.createDirectories(properties.getBaseDir());
        } catch (IOException e) {
            throw new IllegalStateException("Cannot create storage directory", e);
        }
    }

    @Override
    @Transactional
    public StoredFile save(MultipartFile file) throws IOException {
        String cleanName = StringUtils.cleanPath(file.getOriginalFilename() == null ? "file" : file.getOriginalFilename());
        byte[] bytes = file.getBytes();
        return persist(bytes, cleanName, file.getContentType());
    }

    @Override
    @Transactional
    public StoredFile save(byte[] content, String fileName, String mimeType) throws IOException {
        String cleanName = StringUtils.hasText(fileName) ? StringUtils.cleanPath(fileName) : "converted-file";
        return persist(content, cleanName, mimeType);
    }

    private StoredFile persist(byte[] content, String fileName, String mimeType) throws IOException {
        UUID id = UUID.randomUUID();
        String generatedName = id + "-" + fileName;
        Path target = properties.getBaseDir().resolve(generatedName);
        Files.createDirectories(target.getParent());
        Files.write(target, content);
        StoredFile storedFile = new StoredFile(id, fileName, generatedName, content.length, mimeType == null ? "application/octet-stream" : mimeType);
        storedFile.setCreatedAt(Instant.now());
        return storedFileRepository.save(storedFile);
    }

    @Override
    public Resource loadAsResource(UUID fileId) {
        StoredFile storedFile = getMetadata(fileId);
        return new FileSystemResource(resolvePath(storedFile));
    }

    @Override
    public StoredFile getMetadata(UUID fileId) {
        return storedFileRepository.findById(fileId)
                .orElseThrow(() -> new NotFoundException("File %s not found".formatted(fileId)));
    }

    @Override
    public Path resolvePath(StoredFile storedFile) {
        return properties.getBaseDir().resolve(storedFile.getStoragePath());
    }
}

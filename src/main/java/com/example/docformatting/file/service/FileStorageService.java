package com.example.docformatting.file.service;

import com.example.docformatting.file.entity.StoredFile;
import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    StoredFile save(MultipartFile file) throws IOException;

    StoredFile save(byte[] content, String fileName, String mimeType) throws IOException;

    Resource loadAsResource(UUID fileId);

    StoredFile getMetadata(UUID fileId);

    Path resolvePath(StoredFile storedFile);
}

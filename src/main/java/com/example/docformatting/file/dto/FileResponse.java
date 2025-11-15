package com.example.docformatting.file.dto;

import java.util.UUID;

public record FileResponse(UUID fileId, String fileName, String mimeType, long sizeInBytes, String downloadUrl) {
}

package com.example.docformatting.api.dto;

import java.util.UUID;

public record ConversionResponse(UUID fileId, String downloadUrl, String message) {
}

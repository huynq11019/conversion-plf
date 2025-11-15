package com.example.docformatting.converter;

import com.example.docformatting.file.entity.StoredFile;
import java.util.Map;

public record ConversionRequest(ConversionType type, StoredFile inputFile, Map<String, Object> options, String dataPayload) {
}

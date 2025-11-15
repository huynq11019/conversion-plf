package com.example.docformatting.converter;

import com.example.docformatting.file.entity.StoredFile;

public record ConversionResult(StoredFile outputFile, String message) {
}

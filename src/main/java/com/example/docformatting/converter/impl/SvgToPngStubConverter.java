package com.example.docformatting.converter.impl;

import com.example.docformatting.converter.ConversionRequest;
import com.example.docformatting.converter.ConversionResult;
import com.example.docformatting.converter.ConversionType;
import com.example.docformatting.converter.Converter;
import com.example.docformatting.file.entity.StoredFile;
import com.example.docformatting.file.service.FileStorageService;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class SvgToPngStubConverter implements Converter {

    private final FileStorageService fileStorageService;

    public SvgToPngStubConverter(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @Override
    public boolean supports(ConversionType type) {
        return ConversionType.SVG_TO_PNG == type;
    }

    @Override
    public ConversionResult convert(ConversionRequest request) {
        try {
            StoredFile inputFile = request.inputFile();
            byte[] payload;
            try (InputStream in = fileStorageService.loadAsResource(inputFile.getId()).getInputStream()) {
                payload = in.readAllBytes();
            }
            String suggestedName = deriveOutputName(inputFile.getFileName(), request.options());
            StoredFile output = fileStorageService.save(payload, suggestedName, "image/png");
            return new ConversionResult(output, "Stub conversion completed");
        } catch (IOException e) {
            throw new IllegalStateException("Unable to perform stub conversion", e);
        }
    }

    private String deriveOutputName(String originalName, Map<String, Object> options) {
        String customName = options != null ? (String) options.get("outputName") : null;
        if (StringUtils.hasText(customName)) {
            return customName.endsWith(".png") ? customName : customName + ".png";
        }
        if (!StringUtils.hasText(originalName)) {
            return "converted.png";
        }
        String cleaned = originalName.replaceAll("\\.svg$", "");
        return cleaned + ".png";
    }
}

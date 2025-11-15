package com.example.docformatting.api.controller;

import com.example.docformatting.api.dto.ConversionResponse;
import com.example.docformatting.common.dto.ApiResponse;
import com.example.docformatting.common.exception.ValidationException;
import com.example.docformatting.converter.ConversionDispatcher;
import com.example.docformatting.converter.ConversionRequest;
import com.example.docformatting.converter.ConversionResult;
import com.example.docformatting.converter.ConversionType;
import com.example.docformatting.file.entity.StoredFile;
import com.example.docformatting.file.service.FileStorageService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.constraints.NotNull;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/convert")
@Validated
public class ConversionController {

    private final FileStorageService fileStorageService;
    private final ConversionDispatcher conversionDispatcher;
    private final ObjectMapper objectMapper;

    public ConversionController(FileStorageService fileStorageService,
                                ConversionDispatcher conversionDispatcher,
                                ObjectMapper objectMapper) {
        this.fileStorageService = fileStorageService;
        this.conversionDispatcher = conversionDispatcher;
        this.objectMapper = objectMapper;
    }

    @PostMapping(value = "/{type}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ConversionResponse> convert(@PathVariable("type") ConversionType type,
                                                   @RequestPart("file") @NotNull MultipartFile file,
                                                   @RequestPart(value = "options", required = false) String options,
                                                   @RequestPart(value = "data", required = false) String data) throws IOException {
        if (file.isEmpty()) {
            throw new ValidationException("File must not be empty");
        }
        StoredFile inputFile = fileStorageService.save(file);
        Map<String, Object> optionMap = parseOptions(options);
        ConversionRequest request = new ConversionRequest(type, inputFile, optionMap, data);
        ConversionResult result = conversionDispatcher.dispatch(request);
        String downloadUrl = "/api/v1/files/" + result.outputFile().getId();
        ConversionResponse response = new ConversionResponse(result.outputFile().getId(), downloadUrl, result.message());
        return ApiResponse.success(response);
    }

    private Map<String, Object> parseOptions(String optionsJson) {
        if (optionsJson == null || optionsJson.isBlank()) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(optionsJson, new TypeReference<>() { });
        } catch (IOException e) {
            throw new ValidationException("Options JSON không hợp lệ");
        }
    }
}

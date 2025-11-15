package com.example.docformatting.converter;

import com.example.docformatting.common.error.ErrorCode;
import com.example.docformatting.common.exception.BusinessException;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ConversionDispatcher {

    private final List<Converter> converters;

    public ConversionDispatcher(List<Converter> converters) {
        this.converters = converters;
    }

    public ConversionResult dispatch(ConversionRequest request) {
        return converters.stream()
                .filter(converter -> converter.supports(request.type()))
                .findFirst()
                .map(converter -> converter.convert(request))
                .orElseThrow(() -> new BusinessException(ErrorCode.CONVERSION_NOT_SUPPORTED,
                        "Conversion type %s is not supported".formatted(request.type())));
    }
}

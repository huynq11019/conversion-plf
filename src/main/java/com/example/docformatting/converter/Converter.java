package com.example.docformatting.converter;

public interface Converter {

    boolean supports(ConversionType type);

    ConversionResult convert(ConversionRequest request);
}

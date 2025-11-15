package com.example.docformatting.common.exception;

import com.example.docformatting.common.error.ErrorCode;

public class ValidationException extends BusinessException {

    public ValidationException(String message) {
        super(ErrorCode.VALIDATION_ERROR, message);
    }
}

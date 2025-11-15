package com.example.docformatting.common.exception;

import com.example.docformatting.common.error.ErrorCode;

public class NotFoundException extends BusinessException {

    public NotFoundException(String message) {
        super(ErrorCode.NOT_FOUND, message);
    }
}

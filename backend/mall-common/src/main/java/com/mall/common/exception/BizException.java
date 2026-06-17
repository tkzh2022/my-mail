package com.mall.common.exception;

import lombok.Getter;

@Getter
public class BizException extends RuntimeException {

    private final int code;

    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }
}

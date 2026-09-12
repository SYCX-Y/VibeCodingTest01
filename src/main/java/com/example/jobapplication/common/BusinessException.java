package com.example.jobapplication.common;

import lombok.Getter;

/**
 * 业务异常：携带业务错误码
 */
@Getter
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    /** 记录不存在（HTTP 404） */
    public static BusinessException notFound() {
        return new BusinessException(404, "记录不存在");
    }

    /** 非法状态流转（业务错误码 40001） */
    public static BusinessException invalidStatusTransition() {
        return new BusinessException(40001, "不允许的状态流转");
    }
}

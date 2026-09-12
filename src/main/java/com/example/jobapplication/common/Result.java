package com.example.jobapplication.common;

import lombok.Data;

/**
 * 统一接口响应结构
 */
@Data
public class Result<T> {

    /** 0 表示成功，非 0 为业务错误码 */
    private int code;
    private String message;
    private T data;

    public static <T> Result<T> ok() {
        return ok(null);
    }

    public static <T> Result<T> ok(T data) {
        Result<T> r = new Result<>();
        r.code = 0;
        r.message = "success";
        r.data = data;
        return r;
    }

    public static <T> Result<T> error(int code, String message) {
        Result<T> r = new Result<>();
        r.code = code;
        r.message = message;
        return r;
    }
}

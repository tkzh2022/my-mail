package com.mall.common.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class R<T> {

    public static final int SUCCESS_CODE = 0;
    public static final int FAIL_CODE = 500;

    private int code;
    private String message;
    private T data;
    private long timestamp;

    public static <T> R<T> ok() {
        return ok(null);
    }

    public static <T> R<T> ok(T data) {
        R<T> result = new R<>();
        result.setCode(SUCCESS_CODE);
        result.setMessage("success");
        result.setData(data);
        result.setTimestamp(System.currentTimeMillis());
        return result;
    }

    public static <T> R<T> fail(int code, String msg) {
        R<T> result = new R<>();
        result.setCode(code);
        result.setMessage(msg);
        result.setTimestamp(System.currentTimeMillis());
        return result;
    }

    public static <T> R<T> fail(String msg) {
        return fail(FAIL_CODE, msg);
    }
}

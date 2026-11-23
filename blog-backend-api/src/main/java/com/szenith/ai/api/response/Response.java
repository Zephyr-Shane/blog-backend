package com.szenith.ai.api.response;

import com.szenith.ai.types.enums.ResponseCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 统一响应包装，严格遵循 API-CONTRACT v1.0：{code, message, data}。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Response<T> implements Serializable {

    private static final long serialVersionUID = 7000723935764546321L;

    private int code;
    private String message;
    private T data;

    public static <T> Response<T> ok(T data) {
        return new Response<>(ResponseCode.SUCCESS.getCode(), ResponseCode.SUCCESS.getMessage(), data);
    }

    public static <T> Response<T> fail(ResponseCode rc) {
        return new Response<>(rc.getCode(), rc.getMessage(), null);
    }

    public static <T> Response<T> fail(int code, String message) {
        return new Response<>(code, message, null);
    }
}

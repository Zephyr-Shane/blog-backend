package com.szenith.ai.types.exception;

import com.szenith.ai.types.enums.ResponseCode;
import lombok.Getter;

/**
 * 业务异常：全局异常处理据此输出契约响应 {code, message, data:null}。
 */
@Getter
public class AppException extends RuntimeException {

    private static final long serialVersionUID = 5317680961212299217L;

    private final int code;

    public AppException(ResponseCode rc) {
        super(rc.getMessage());
        this.code = rc.getCode();
    }

    public AppException(ResponseCode rc, String message) {
        super(message);
        this.code = rc.getCode();
    }

    public AppException(int code, String message) {
        super(message);
        this.code = code;
    }

    public AppException(ResponseCode rc, Throwable cause) {
        super(rc.getMessage(), cause);
        this.code = rc.getCode();
    }
}

package com.szenith.ai.types.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 契约响应码（API-CONTRACT v1.0）。
 * code 为数字，与前端 ApiResponse.code 严格一致。
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
public enum ResponseCode {

    SUCCESS(200, "ok"),
    BAD_REQUEST(400, "参数错误"),
    UNAUTHORIZED(401, "未登录或 token 失效"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "资源不存在"),
    CONFLICT(409, "资源被引用，无法删除"),
    INTERNAL_ERROR(500, "服务端错误"),
    ;

    private int code;
    private String message;

    public static ResponseCode of(int code) {
        for (ResponseCode rc : values()) {
            if (rc.code == code) return rc;
        }
        return INTERNAL_ERROR;
    }
}

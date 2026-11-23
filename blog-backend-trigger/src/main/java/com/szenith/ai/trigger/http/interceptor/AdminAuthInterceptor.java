package com.szenith.ai.trigger.http.interceptor;

import com.szenith.ai.domain.blog.adapter.port.TokenPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.szenith.ai.api.response.Response;
import com.szenith.ai.types.enums.ResponseCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/** 后台鉴权拦截器：/admin/** 除登录外均需 Bearer Token */
@Component
@RequiredArgsConstructor
public class AdminAuthInterceptor implements HandlerInterceptor {

    private final TokenPort tokenPort;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws java.io.IOException {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true;
        String auth = request.getHeader("Authorization");
        String token = null;
        if (auth != null && auth.startsWith("Bearer ")) {
            token = auth.substring(7).trim();
        }
        if (!tokenPort.validate(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(objectMapper.writeValueAsString(
                    Response.fail(ResponseCode.UNAUTHORIZED)));
            return false;
        }
        return true;
    }
}

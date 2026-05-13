package com.youdao.paper.interceptor;

import com.youdao.paper.common.ResultCode;
import com.youdao.paper.entity.SysUser;
import com.youdao.paper.exception.BusinessException;
import com.youdao.paper.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;

@Component
public class AuthenticationInterceptor implements HandlerInterceptor {

    private static final String CURRENT_USER = "currentUser";

    private static final Set<String> PUBLIC_PATHS = Set.of(
            "/api/auth/register",
            "/api/auth/check-username",
            "/api/auth/check-phone",
            "/api/auth/login/password",
            "/api/auth/login/sms",
            "/api/auth/password/reset",
            "/api/sms/send",
            "/api/admin/login",
            "/api/rewrite/presets"
    );

    private final AuthService authService;

    public AuthenticationInterceptor(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String uri = request.getRequestURI();
        if (PUBLIC_PATHS.contains(uri)) {
            return true;
        }

        String token = request.getHeader("Authorization");
        SysUser user = authService.getLoginUser(token);
        request.setAttribute(CURRENT_USER, user);

        if (uri.startsWith("/api/admin/")) {
            if (!"ADMIN".equals(user.getRole())) {
                throw new BusinessException(ResultCode.FORBIDDEN);
            }
        }
        return true;
    }
}

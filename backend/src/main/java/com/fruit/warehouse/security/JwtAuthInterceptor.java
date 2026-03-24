package com.fruit.warehouse.security;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fruit.warehouse.common.Result;
import com.fruit.warehouse.mapper.SysPermissionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthInterceptor implements HandlerInterceptor {

    private final JwtUtils jwtUtils;
    private final SysPermissionMapper permissionMapper;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String token = extractToken(request);
        if (StrUtil.isBlank(token)) {
            writeError(response, 401, "请先登录");
            return false;
        }

        try {
            if (jwtUtils.isTokenExpired(token)) {
                writeError(response, 401, "登录已过期，请重新登录");
                return false;
            }

            Long userId = jwtUtils.getUserIdFromToken(token);
            String username = jwtUtils.getUsernameFromToken(token);
            List<String> roles = jwtUtils.getRolesFromToken(token);

            UserContext context = new UserContext();
            context.setUserId(userId);
            context.setUsername(username);
            context.setRoles(roles);

            // Load permissions
            List<String> permissionCodes = permissionMapper.selectPermissionCodesByUserId(userId);
            context.setPermissions(new HashSet<>(permissionCodes));

            UserContext.set(context);
            return true;
        } catch (Exception e) {
            log.error("Token validation failed", e);
            writeError(response, 401, "无效的登录凭证");
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StrUtil.isNotBlank(header) && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    private void writeError(HttpServletResponse response, int code, String message) throws IOException {
        response.setStatus(200);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(Result.error(code, message)));
    }
}

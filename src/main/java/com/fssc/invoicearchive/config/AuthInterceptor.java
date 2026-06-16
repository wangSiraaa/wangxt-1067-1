package com.fssc.invoicearchive.config;

import com.fssc.invoicearchive.context.UserContext;
import com.fssc.invoicearchive.entity.SysUser;
import com.fssc.invoicearchive.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    public static final ConcurrentHashMap<String, SysUser> TOKEN_STORE = new ConcurrentHashMap<>();

    @Autowired
    private UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();
        if (path.contains("/api/user/login") || path.contains("/h2-console") ||
            path.equals("/") || path.endsWith(".html") || path.endsWith(".js") ||
            path.endsWith(".css") || path.endsWith(".png") || path.endsWith(".jpg") ||
            path.endsWith(".ico") || path.endsWith(".map")) {
            return true;
        }

        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            token = request.getParameter("token");
        }

        if (token != null && !token.isEmpty()) {
            SysUser user = TOKEN_STORE.get(token);
            if (user != null) {
                UserContext.setCurrentUser(user);
                return true;
            }
        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":401,\"message\":\"未登录或登录已过期\"}");
        return false;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserContext.clear();
    }

    public static String generateToken() {
        return java.util.UUID.randomUUID().toString().replace("-", "");
    }
}

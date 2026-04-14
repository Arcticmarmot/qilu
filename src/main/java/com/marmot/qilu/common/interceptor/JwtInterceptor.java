package com.marmot.qilu.common.interceptor;

import com.marmot.qilu.common.context.UserContext;
import com.marmot.qilu.common.security.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) throws Exception {
        String authorization = request.getHeader("Authorization");

        if(authorization == null || authorization.isBlank()) {
            writeUnauthorized(response, "Authorization header is missing.");
            return false;
        }

        if(!authorization.startsWith("Bearer ")) {
            writeUnauthorized(response, "Invalid authorization header.");
            return false;
        }

        String token = authorization.substring(7);

        try {
            Claims claims = jwtUtil.parseToken(token);
            Date expiration = claims.getExpiration();
            if(expiration == null || expiration.before(new Date())) {
                writeUnauthorized(response, "Token has expired.");
                return false;
            }

            String uuid = claims.getSubject();
            if(uuid == null || uuid.isBlank()) {
                writeUnauthorized(response, "Invalid token subject.");
                return false;
            }

            UserContext.setUuid(uuid);
            return true;
        } catch (Exception e) {
            writeUnauthorized(response, "Invalid token.");
            return false;
        }
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request,
                                @NonNull HttpServletResponse response,
                                @NonNull Object handler,
                                Exception ex) {
        UserContext.clear();
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws Exception {
        response.setStatus(401);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(
                "{\"code\":401, \"message\": \"" + message + "\", \"data\":null}"
        );
    }
}

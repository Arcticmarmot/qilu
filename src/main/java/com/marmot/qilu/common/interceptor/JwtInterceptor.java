package com.marmot.qilu.common.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marmot.qilu.common.api.ApiResponse;
import com.marmot.qilu.common.api.ErrorCode;
import com.marmot.qilu.common.context.UserContext;
import com.marmot.qilu.common.security.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;


    @Override
    public boolean preHandle(HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String authorization = request.getHeader(AUTHORIZATION_HEADER);

        if(authorization == null || authorization.isBlank()) {
            writeUnauthorized(response, "authorization header is missing");
            return false;
        }

        if(!authorization.startsWith(BEARER_PREFIX)) {
            writeUnauthorized(response, "authorization header is invalid");
            return false;
        }

        String token = authorization.substring(BEARER_PREFIX.length());

        try {
            Claims claims = jwtUtil.parseToken(token);

            Date expiration = claims.getExpiration();
            if(expiration == null || expiration.before(new Date())) {
                writeUnauthorized(response, "token has expired");
                return false;
            }

            String uuid = claims.getSubject();
            if(uuid == null || uuid.isBlank()) {
                writeUnauthorized(response, "token subject is invalid");
                return false;
            }

            UserContext.setUuid(uuid);
            return true;
        } catch (Exception e) {
            log.debug(
                    "parse jwt failed, method={}, uri={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    e
            );

            writeUnauthorized(response, "invalid token");
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
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");
        ApiResponse<Void> body = ApiResponse.fail(ErrorCode.UNAUTHORIZED, message);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}

package com.gym.interceptor;

import com.gym.exception.AuthenticationException;
import com.gym.service.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@Slf4j
public class AuthenticationInterceptor implements HandlerInterceptor {

    private AuthenticationService authenticationService;

    @Autowired
    public void setAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        String username = request.getHeader("Username");
        String password = request.getHeader("Password");

        if (username == null || username.isBlank()) {
            log.warn("Request rejected — missing Username header | uri={}",
                    request.getRequestURI());
            throw new AuthenticationException("Username header is required");
        }

        if (password == null || password.isBlank()) {
            log.warn("Request rejected — missing Password header | uri={}",
                    request.getRequestURI());
            throw new AuthenticationException("Password header is required");
        }

        log.debug("Authenticating request | username={} | uri={}",
                username, request.getRequestURI());

        authenticationService.authenticate(username, password);

        log.debug("Authentication passed | username={}", username);
        return true;
    }
}

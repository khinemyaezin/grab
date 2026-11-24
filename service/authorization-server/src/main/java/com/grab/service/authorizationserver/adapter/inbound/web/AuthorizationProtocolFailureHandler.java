package com.grab.service.authorizationserver.adapter.inbound.web;

import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.server.authorization.web.authentication.OAuth2ErrorAuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import java.io.IOException;

public class AuthorizationProtocolFailureHandler implements AuthenticationFailureHandler {

    private static final Logger log = Loggers.getLogger(AuthorizationProtocolFailureHandler.class);

    private final AuthenticationFailureHandler defaults = new OAuth2ErrorAuthenticationFailureHandler();

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        if (exception instanceof OAuth2AuthenticationException protocol
                && OAuth2ErrorCodes.SERVER_ERROR.equals(protocol.getError().getErrorCode())) {
            log.error("OAuth2 server error during authorization on {}: {}",
                    request.getRequestURI(), exception.getMessage(), exception);
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"error\":\"server_error\"}");
        } else {
            log.warn("OAuth2 authentication rejected on {}: {}",
                    request.getRequestURI(), exception.getMessage());
            defaults.onAuthenticationFailure(request, response, exception);
        }
    }
}

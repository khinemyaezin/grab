package com.grab.service.authorizationserver.core.user;

import com.grab.service.authorizationserver.adapter.inbound.web.ContextSelectionFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class ContextAwareOAuth2AuthorizationService implements OAuth2AuthorizationService {
    private final OAuth2AuthorizationService delegate;

    public ContextAwareOAuth2AuthorizationService(OAuth2AuthorizationService delegate) {
        this.delegate = delegate;
    }

    @Override
    public void save(OAuth2Authorization authorization) {
        OAuth2Authorization toSave = authorization;
        if (toSave != null && AuthorizationGrantType.AUTHORIZATION_CODE.equals(toSave.getAuthorizationGrantType())) {
            RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
            if (requestAttributes instanceof ServletRequestAttributes servletAttrs) {
                HttpServletRequest request = servletAttrs.getRequest();
                HttpSession session = request.getSession(false);
                if (session != null) {
                    SelectedContext selected = (SelectedContext) session.getAttribute(ContextSelectionFilter.SESSION_ATTR_SELECTED_CONTEXT);
                    if (selected != null) {
                        toSave = selected.storeIn(toSave);
                    }
                }
            }
        }
        delegate.save(toSave);
    }

    @Override
    public void remove(OAuth2Authorization authorization) {
        delegate.remove(authorization);
    }

    @Override
    public OAuth2Authorization findById(String id) {
        return delegate.findById(id);
    }

    @Override
    public OAuth2Authorization findByToken(String token, OAuth2TokenType tokenType) {
        return delegate.findByToken(token, tokenType);
    }
}

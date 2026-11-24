package com.grab.service.authorizationserver.adapter.inbound.web;

import com.grab.service.authorizationserver.core.user.ContextSelectionService;
import com.grab.service.authorizationserver.core.user.IamAccessContext;
import com.grab.service.authorizationserver.core.user.IamUser;
import com.grab.service.authorizationserver.core.user.IamUserRepository;
import com.grab.service.authorizationserver.core.user.SelectedContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class ContextSelectionFilter extends OncePerRequestFilter {
    public static final String SESSION_ATTR_SELECTED_CONTEXT = "SELECTED_ACCESS_CONTEXT";

    private final ContextSelectionService contextService;
    private final IamUserRepository users;
    private final RequestCache requestCache = new HttpSessionRequestCache();

    public ContextSelectionFilter(ContextSelectionService contextService, IamUserRepository users) {
        this.contextService = contextService;
        this.users = users;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getServletPath();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if ("/oauth2/authorize".equals(path) && auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            HttpSession session = request.getSession(true);

            String prompt = request.getParameter("prompt");
            if (prompt != null && prompt.contains("select_context")) {
                session.removeAttribute(SESSION_ATTR_SELECTED_CONTEXT);
            }

            SelectedContext selected = (SelectedContext) session.getAttribute(SESSION_ATTR_SELECTED_CONTEXT);
            if (selected == null) {
                String iamUserId = auth.getName();
                IamUser user = users.findById(iamUserId).orElse(null);
                String platformUserId = user != null ? user.platformUserId() : iamUserId;

                List<IamAccessContext> available = contextService.getOrSyncContexts(iamUserId);

                if (available.isEmpty()) {
                    SelectedContext onboarding = SelectedContext.onboarding(platformUserId);
                    session.setAttribute(SESSION_ATTR_SELECTED_CONTEXT, onboarding);
                } else if (available.size() == 1) {
                    IamAccessContext single = available.getFirst();
                    SelectedContext auto = new SelectedContext(
                            single.assignmentId(),
                            platformUserId,
                            single.scopeKey(),
                            single.scopeId(),
                            single.role(),
                            single.displayName(),
                            single.authorities(),
                            false
                    );
                    session.setAttribute(SESSION_ATTR_SELECTED_CONTEXT, auto);
                } else {
                    requestCache.saveRequest(request, response);
                    response.sendRedirect(request.getContextPath() + "/oauth2/select-context");
                    return;
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}

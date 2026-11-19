package com.grab.service.authorizationserver.adapter.inbound.web;

import com.grab.service.authorizationserver.core.user.ContextSelectionService;
import com.grab.service.authorizationserver.core.user.IamAccessContext;
import com.grab.service.authorizationserver.core.user.IamUser;
import com.grab.service.authorizationserver.core.user.IamUserRepository;
import com.grab.service.authorizationserver.core.user.SelectedContext;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ContextSelectionFilterTest {
    private ContextSelectionService contextService;
    private IamUserRepository userRepo;
    private ContextSelectionFilter filter;

    @BeforeEach
    void setUp() {
        contextService = mock(ContextSelectionService.class);
        userRepo = mock(IamUserRepository.class);
        filter = new ContextSelectionFilter(contextService, userRepo);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void ignoresNonAuthorizePaths() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/login");
        request.setServletPath("/login");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verifyNoInteractions(contextService);
    }

    @Test
    void ignoresUnauthenticatedRequests() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/oauth2/authorize");
        request.setServletPath("/oauth2/authorize");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verifyNoInteractions(contextService);
    }

    @Test
    void autoSelectsOnboardingWhenUserHasZeroContexts() throws Exception {
        String userId = "iam-user-new";
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(userId, null, List.of())
        );

        IamUser user = new IamUser(userId, "newbie", "new@example.com", "hash", "New", "User",
                true, "platform-new", 1L, Set.of(), Instant.now(), Instant.now());
        when(userRepo.findById(userId)).thenReturn(Optional.of(user));
        when(contextService.getOrSyncContexts(userId)).thenReturn(List.of());

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/oauth2/authorize");
        request.setServletPath("/oauth2/authorize");
        MockHttpSession session = new MockHttpSession();
        request.setSession(session);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        SelectedContext selected = (SelectedContext) session.getAttribute(ContextSelectionFilter.SESSION_ATTR_SELECTED_CONTEXT);
        assertThat(selected).isNotNull();
        assertThat(selected.onboarding()).isTrue();
        assertThat(selected.platformUserId()).isEqualTo("platform-new");
    }

    @Test
    void autoSelectsSingleContextWhenUserHasExactlyOne() throws Exception {
        String userId = "iam-user-solo";
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(userId, null, List.of())
        );

        IamUser user = new IamUser(userId, "solo", "solo@example.com", "hash", "Solo", "User",
                true, "platform-solo", 1L, Set.of(), Instant.now(), Instant.now());
        when(userRepo.findById(userId)).thenReturn(Optional.of(user));

        IamAccessContext single = new IamAccessContext("ctx-1", userId, "assign-1", "merchant.account",
                "merchant-1", "MERCHANT_OWNER", "Solo Merchant", Set.of("orders:read"), Instant.now());
        when(contextService.getOrSyncContexts(userId)).thenReturn(List.of(single));

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/oauth2/authorize");
        request.setServletPath("/oauth2/authorize");
        MockHttpSession session = new MockHttpSession();
        request.setSession(session);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        SelectedContext selected = (SelectedContext) session.getAttribute(ContextSelectionFilter.SESSION_ATTR_SELECTED_CONTEXT);
        assertThat(selected).isNotNull();
        assertThat(selected.onboarding()).isFalse();
        assertThat(selected.assignmentId()).isEqualTo("assign-1");
        assertThat(selected.scopeKey()).isEqualTo("merchant.account");
        assertThat(selected.scopeId()).isEqualTo("merchant-1");
        assertThat(selected.role()).isEqualTo("MERCHANT_OWNER");
    }

    @Test
    void redirectsToSelectContextPromptWhenUserHasMultipleContexts() throws Exception {
        String userId = "iam-user-multi";
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(userId, null, List.of())
        );

        IamUser user = new IamUser(userId, "multi", "multi@example.com", "hash", "Multi", "User",
                true, "platform-multi", 1L, Set.of(), Instant.now(), Instant.now());
        when(userRepo.findById(userId)).thenReturn(Optional.of(user));

        IamAccessContext ctx1 = new IamAccessContext("c1", userId, "assign-1", "merchant.account",
                "m-1", "MERCHANT_OWNER", "Merchant 1", Set.of(), Instant.now());
        IamAccessContext ctx2 = new IamAccessContext("c2", userId, "assign-2", "merchant.storefront",
                "s-2", "STORE_MANAGER", "Store 2", Set.of(), Instant.now());
        when(contextService.getOrSyncContexts(userId)).thenReturn(List.of(ctx1, ctx2));

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/oauth2/authorize");
        request.setServletPath("/oauth2/authorize");
        MockHttpSession session = new MockHttpSession();
        request.setSession(session);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(response.getRedirectedUrl()).isEqualTo("/oauth2/select-context");
        assertThat(session.getAttribute(ContextSelectionFilter.SESSION_ATTR_SELECTED_CONTEXT)).isNull();
    }

    @Test
    void clearsExistingContextWhenPromptSelectContextIsRequested() throws Exception {
        String userId = "iam-user-prompt";
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(userId, null, List.of())
        );

        IamUser user = new IamUser(userId, "prompt", "prompt@example.com", "hash", "Prompt", "User",
                true, "platform-prompt", 1L, Set.of(), Instant.now(), Instant.now());
        when(userRepo.findById(userId)).thenReturn(Optional.of(user));

        IamAccessContext ctx1 = new IamAccessContext("c1", userId, "assign-1", "merchant.account",
                "m-1", "MERCHANT_OWNER", "Merchant 1", Set.of(), Instant.now());
        IamAccessContext ctx2 = new IamAccessContext("c2", userId, "assign-2", "merchant.storefront",
                "s-2", "STORE_MANAGER", "Store 2", Set.of(), Instant.now());
        when(contextService.getOrSyncContexts(userId)).thenReturn(List.of(ctx1, ctx2));

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/oauth2/authorize");
        request.setServletPath("/oauth2/authorize");
        request.setParameter("prompt", "select_context");
        MockHttpSession session = new MockHttpSession();
        SelectedContext prior = new SelectedContext("assign-1", "platform-prompt", "merchant.account",
                "m-1", "MERCHANT_OWNER", "Merchant 1", Set.of(), false);
        session.setAttribute(ContextSelectionFilter.SESSION_ATTR_SELECTED_CONTEXT, prior);
        request.setSession(session);

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        // Prompt clears the old context and since there are 2 contexts, redirects to selection
        assertThat(response.getRedirectedUrl()).isEqualTo("/oauth2/select-context");
    }
}

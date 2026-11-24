package com.grab.service.authorizationserver.adapter.inbound.web;

import com.grab.service.authorizationserver.core.user.ContextSelectionService;
import com.grab.service.authorizationserver.core.user.IamAccessContext;
import com.grab.service.authorizationserver.core.user.IamUser;
import com.grab.service.authorizationserver.core.user.IamUserRepository;
import com.grab.service.authorizationserver.core.user.SelectedContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ContextSelectionControllerTest {
    private ContextSelectionService contextService;
    private IamUserRepository userRepository;
    private ContextSelectionController controller;

    @BeforeEach
    void setUp() {
        contextService = mock(ContextSelectionService.class);
        userRepository = mock(IamUserRepository.class);
        controller = new ContextSelectionController(contextService, userRepository);
    }

    @Test
    void redirectsUnauthenticatedToLogin() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        Model model = new ConcurrentModel();

        String view = controller.showSelectContext(null, request, response, model);
        assertThat(view).isEqualTo("redirect:/login");

        String postView = controller.handleSelectContext("a1", null, request, response);
        assertThat(postView).isEqualTo("redirect:/login");
    }

    @Test
    void showsSelectContextPageWhenMultipleContextsExist() {
        String userId = "iam-user-1";
        Authentication auth = UsernamePasswordAuthenticationToken.authenticated(userId, null, List.of());
        IamUser user = new IamUser(userId, "seller", "seller@example.com", "hash", "Bob", "Smith",
                true, "p-1", 1L, Set.of(), Instant.now(), Instant.now());
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        IamAccessContext c1 = new IamAccessContext("ctx-1", userId, "a1", "merchant.account", "m-1",
                "MERCHANT_OWNER", "Store 1", Set.of(), Instant.now());
        IamAccessContext c2 = new IamAccessContext("ctx-2", userId, "a2", "merchant.account", "m-2",
                "STORE_MANAGER", "Store 2", Set.of(), Instant.now());
        when(contextService.getOrSyncContexts(userId)).thenReturn(List.of(c1, c2));

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        Model model = new ConcurrentModel();

        String view = controller.showSelectContext(auth, request, response, model);

        assertThat(view).isEqualTo("select-context");
        assertThat(model.getAttribute("user")).isEqualTo(user);
        assertThat(model.getAttribute("contexts")).isEqualTo(List.of(c1, c2));
    }

    @Test
    void handlesSelectContextPostAndSavesToSession() {
        String userId = "iam-user-1";
        Authentication auth = UsernamePasswordAuthenticationToken.authenticated(userId, null, List.of());

        SelectedContext selected = new SelectedContext("a2", "p-1", "merchant.account", "m-2",
                "STORE_MANAGER", "Store 2", Set.of(), false);
        when(contextService.resolveSelection(userId, "a2")).thenReturn(selected);

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpSession session = new MockHttpSession();
        request.setSession(session);
        MockHttpServletResponse response = new MockHttpServletResponse();

        String view = controller.handleSelectContext("a2", auth, request, response);

        assertThat(view).isEqualTo("redirect:/");
        assertThat(session.getAttribute(ContextSelectionFilter.SESSION_ATTR_SELECTED_CONTEXT)).isEqualTo(selected);
    }
}

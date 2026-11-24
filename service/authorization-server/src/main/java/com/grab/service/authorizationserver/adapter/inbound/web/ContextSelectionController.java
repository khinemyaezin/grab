package com.grab.service.authorizationserver.adapter.inbound.web;

import com.grab.service.authorizationserver.core.user.ContextSelectionService;
import com.grab.service.authorizationserver.core.user.IamAccessContext;
import com.grab.service.authorizationserver.core.user.IamUser;
import com.grab.service.authorizationserver.core.user.IamUserRepository;
import com.grab.service.authorizationserver.core.user.SelectedContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class ContextSelectionController {
    private final ContextSelectionService contextService;
    private final IamUserRepository users;
    private final RequestCache requestCache = new HttpSessionRequestCache();

    public ContextSelectionController(ContextSelectionService contextService, IamUserRepository users) {
        this.contextService = contextService;
        this.users = users;
    }

    @GetMapping("/oauth2/select-context")
    public String showSelectContext(Authentication auth,
                                   HttpServletRequest request,
                                   HttpServletResponse response,
                                   Model model) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }
        String iamUserId = auth.getName();
        IamUser user = users.findById(iamUserId).orElseThrow();
        List<IamAccessContext> contexts = contextService.getOrSyncContexts(iamUserId);

        if (contexts.size() <= 1) {
            String platformUserId = user.platformUserId() != null ? user.platformUserId() : user.id();
            SelectedContext selected = contexts.isEmpty()
                    ? SelectedContext.onboarding(platformUserId)
                    : new SelectedContext(contexts.getFirst().assignmentId(), platformUserId,
                    contexts.getFirst().scopeKey(), contexts.getFirst().scopeId(),
                    contexts.getFirst().role(), contexts.getFirst().displayName(),
                    contexts.getFirst().authorities(), false);

            request.getSession().setAttribute(ContextSelectionFilter.SESSION_ATTR_SELECTED_CONTEXT, selected);
            return redirectToSaved(request, response);
        }

        model.addAttribute("contexts", contexts);
        model.addAttribute("user", user);
        return "select-context";
    }

    @PostMapping("/oauth2/select-context")
    public String handleSelectContext(@RequestParam("assignmentId") String assignmentId,
                                     Authentication auth,
                                     HttpServletRequest request,
                                     HttpServletResponse response) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }
        String iamUserId = auth.getName();
        SelectedContext selected = contextService.resolveSelection(iamUserId, assignmentId);
        HttpSession session = request.getSession(true);
        session.setAttribute(ContextSelectionFilter.SESSION_ATTR_SELECTED_CONTEXT, selected);

        return redirectToSaved(request, response);
    }

    private String redirectToSaved(HttpServletRequest request, HttpServletResponse response) {
        SavedRequest savedRequest = requestCache.getRequest(request, response);
        if (savedRequest != null) {
            return "redirect:" + savedRequest.getRedirectUrl();
        }
        return "redirect:/";
    }
}

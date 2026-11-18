package com.grab.service.sellerbff.port.inbound;

import com.grab.framework.security.AuthenticatedActor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public interface SellerSessionPort {
    AuthenticatedActor select(HttpServletRequest request, String subject, String assignmentId);

    void invalidate(HttpSession session);
}

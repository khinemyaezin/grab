package com.grab.service.sellerbff.port.outbound;

import java.util.Set;

public interface SellerAccessOutboundPort {
    AccessState resolve(String issuer, String subject, String assignmentId, Intent intent);

    enum Intent { INITIAL, REVALIDATE, SELECT }

    enum Status { ACTIVE, UNKNOWN_IDENTITY, INACTIVE_IDENTITY, INVALID_CONTEXT, DENIED }

    record AccessState(Status status, String userId, String email, long authenticationVersion,
                       String assignmentId, String scopeKey, String scopeId, Set<String> roles,
                       Set<String> authorities, long merchantRevision, long storefrontRevision) {}
}

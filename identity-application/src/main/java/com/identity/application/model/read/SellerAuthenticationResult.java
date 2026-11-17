package com.identity.application.model.read;

import java.util.Set;

public record SellerAuthenticationResult(String userId, String email, String status, long authenticationVersion,
                                         String assignmentId, String scopeKey, String scopeId, Set<String> roles,
                                         Set<String> authorities, long merchantRevision, long storefrontRevision) {}

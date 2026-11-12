package com.grab.app.platform.seller.internal.adapter.inbound.rest.service;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.CurrentUserProfileResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.UserProfileDetailResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.security.principal.SellerSecurityPrincipal;
import com.grab.app.platform.seller.internal.query.model.SellerIdentityAdminQueries.GetUserDetailQuery;
import com.grab.framework.cqrs.query.QueryBus;
import com.grab.framework.security.AccessContext;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SellerProfileQueryService {

    private final QueryBus queryBus;

    public SellerProfileQueryService(@Qualifier("sellerQueryBus") QueryBus queryBus) {
        this.queryBus = queryBus;
    }

    public Optional<CurrentUserProfileResponse> getProfile(SellerSecurityPrincipal principal) {
        if (principal == null) {
            return Optional.empty();
        }
        String userId = principal.getPlatformUserId();
        Optional<UserProfileDetailResponse> detailOpt = queryBus.dispatch(new GetUserDetailQuery(userId));

        return detailOpt.map(detail -> {
            List<CurrentUserProfileResponse.AccessContextInfo> contexts = (detail.accessContexts() == null)
                    ? List.of()
                    : detail.accessContexts().stream()
                    .map(c -> new CurrentUserProfileResponse.AccessContextInfo(
                            c.assignmentId(),
                            c.roleCode(),
                            c.scopeKey(),
                            c.scopeId(),
                            c.status()
                    ))
                    .toList();

            CurrentUserProfileResponse.AccessContextInfo currentContext = principal.getAccessContext()
                    .map(ac -> new CurrentUserProfileResponse.AccessContextInfo(
                            ac.assignmentId(),
                            principal.getAuthorities().stream().findFirst().map(Object::toString).orElse(""),
                            ac.scopeKey(),
                            ac.scopeId(),
                            "ACTIVE"
                    ))
                    .orElse(null);

            return new CurrentUserProfileResponse(
                    detail.id(),
                    detail.email(),
                    detail.status(),
                    detail.createdAt(),
                    contexts,
                    currentContext
            );
        });
    }
}


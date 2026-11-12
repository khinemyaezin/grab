package com.grab.app.platform.seller.port.rest;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.AccessContextResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.CurrentUserProfileResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.SellerAuthResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.security.principal.SellerSecurityPrincipal;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;

public interface SellerSessionResource {

    ResponseEntity<EntityModel<CurrentUserProfileResponse>> getProfile(
            SellerSecurityPrincipal principal
    );

    ResponseEntity<CollectionModel<EntityModel<AccessContextResponse>>> listContexts(
            SellerSecurityPrincipal principal
    );

    ResponseEntity<EntityModel<SellerAuthResponse>> selectContext(
            String assignmentId,
            String currentRefreshToken,
            SellerSecurityPrincipal principal
    );
}


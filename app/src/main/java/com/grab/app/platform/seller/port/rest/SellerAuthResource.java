package com.grab.app.platform.seller.port.rest;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.SellerLoginRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.SellerLogoutRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.SellerRefreshTokenRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.SellerRegisterRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.SellerAuthResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.SellerUserProfileResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;

/**
 * Public REST Contract for Seller Authentication and Session Management.
 */
public interface SellerAuthResource {

    ResponseEntity<EntityModel<SellerAuthResponse>> login(SellerLoginRequest request);

    ResponseEntity<EntityModel<SellerAuthResponse>> refresh(String refreshTokenCookie, SellerRefreshTokenRequest request);

    ResponseEntity<Void> logout(String refreshTokenCookie, SellerLogoutRequest request);

    ResponseEntity<EntityModel<SellerUserProfileResponse>> register(SellerRegisterRequest request);
}


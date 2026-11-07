package com.grab.app.platform.seller.port.rest;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.*;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.MerchantMemberResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.MerchantResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.StorefrontResponse;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;

/**
 * Public REST Contract for Merchant Accounts, Storefronts, and Team Members.
 */
public interface SellerMerchantResource {

    ResponseEntity<EntityModel<MerchantResponse>> startApplication(StartMerchantApplicationRequest request, String actorId);

    ResponseEntity<EntityModel<MerchantResponse>> submitApplication(String applicationId, String actorId);

    ResponseEntity<EntityModel<MerchantResponse>> getMerchant(String merchantId, String actorId);

    ResponseEntity<CollectionModel<EntityModel<MerchantResponse>>> listMyMerchants(String actorId);

    ResponseEntity<EntityModel<MerchantResponse>> updateProfile(String merchantId, UpdateMerchantProfileRequest request, String actorId);

    /**
     * Public REST Contract for Merchant Branded Storefronts.
     */
    interface Storefronts {
        ResponseEntity<EntityModel<StorefrontResponse>> createStorefront(String merchantId, CreateStorefrontRequest request);
        ResponseEntity<EntityModel<StorefrontResponse>> getStorefront(String merchantId, String storefrontId);
        ResponseEntity<CollectionModel<EntityModel<StorefrontResponse>>> listStorefronts(String merchantId);
        ResponseEntity<EntityModel<StorefrontResponse>> updateStorefront(String merchantId, String storefrontId, UpdateStorefrontRequest request);
        ResponseEntity<Void> activateStorefront(String merchantId, String storefrontId);
        ResponseEntity<Void> closeStorefront(String merchantId, String storefrontId);
    }

    /**
     * Public REST Contract for Merchant Team Members and Roles.
     */
    interface Members {
        ResponseEntity<EntityModel<MerchantMemberResponse>> inviteMember(String merchantId, InviteMemberRequest request, String actorId);
        ResponseEntity<EntityModel<MerchantMemberResponse>> acceptInvitation(String merchantId, String memberId, String actorId);
        ResponseEntity<EntityModel<MerchantMemberResponse>> updateRole(String merchantId, String memberId, UpdateMemberRoleRequest request, String actorId);
        ResponseEntity<Void> removeMember(String merchantId, String memberId, String actorId);
        ResponseEntity<CollectionModel<EntityModel<MerchantMemberResponse>>> listMembers(String merchantId);
    }
}

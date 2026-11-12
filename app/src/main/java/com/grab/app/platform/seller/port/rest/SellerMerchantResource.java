package com.grab.app.platform.seller.port.rest;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.CreateStorefrontRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.InviteMemberRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.MerchantLifecycleRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.StartMerchantApplicationRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.UpdateMemberRoleRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.UpdateMerchantProfileRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.UpdateStorefrontRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.GetC2CApplicationResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.GetFirstPartyRetailerApplicationResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.MerchantMemberResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.MerchantResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.StorefrontResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.security.principal.SellerSecurityPrincipal;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;

/**
 * Public REST Contract for Merchant Accounts, Applications, Storefronts, Team Members, and Admin Lifecycle.
 */
public interface SellerMerchantResource {

    // --- Core Merchant Account & Profile ---
    ResponseEntity<EntityModel<MerchantResponse>> getMerchant(String merchantId, String actorId);

    ResponseEntity<CollectionModel<EntityModel<MerchantResponse>>> listMyMerchants(String actorId);

    ResponseEntity<EntityModel<MerchantResponse>> updateProfile(String merchantId, UpdateMerchantProfileRequest request, String actorId);

    /**
     * Public REST Contract for Merchant Onboarding and Applications.
     */
    interface Applications {
        ResponseEntity<EntityModel<MerchantResponse>> startApplication(StartMerchantApplicationRequest request, String actorId);

        ResponseEntity<EntityModel<MerchantResponse>> submitApplication(String applicationId, String actorId);

        ResponseEntity<EntityModel<GetC2CApplicationResponse>> getC2CApplication(SellerSecurityPrincipal principal);

        ResponseEntity<EntityModel<MerchantResponse>> startC2CApplication(
                StartMerchantApplicationRequest request,
                SellerSecurityPrincipal principal
        );

        ResponseEntity<EntityModel<GetFirstPartyRetailerApplicationResponse>> getFirstPartyApplication(SellerSecurityPrincipal principal);

        ResponseEntity<EntityModel<MerchantResponse>> startFirstPartyApplication(
                StartMerchantApplicationRequest request,
                SellerSecurityPrincipal principal
        );
    }

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

    /**
     * Public REST Contract for Platform Merchant Lifecycle Administration.
     */
    interface Admin {
        ResponseEntity<CollectionModel<EntityModel<MerchantResponse>>> reviewQueue(String status);

        ResponseEntity<EntityModel<MerchantResponse>> requestChanges(
                String merchantId,
                MerchantLifecycleRequest request,
                SellerSecurityPrincipal principal
        );

        ResponseEntity<EntityModel<MerchantResponse>> approve(
                String merchantId,
                SellerSecurityPrincipal principal
        );

        ResponseEntity<EntityModel<MerchantResponse>> reject(
                String merchantId,
                MerchantLifecycleRequest request,
                SellerSecurityPrincipal principal
        );

        ResponseEntity<EntityModel<MerchantResponse>> suspend(
                String merchantId,
                MerchantLifecycleRequest request,
                SellerSecurityPrincipal principal
        );

        ResponseEntity<EntityModel<MerchantResponse>> reactivate(
                String merchantId,
                SellerSecurityPrincipal principal
        );

        ResponseEntity<EntityModel<MerchantResponse>> close(
                String merchantId,
                MerchantLifecycleRequest request,
                SellerSecurityPrincipal principal
        );
    }
}

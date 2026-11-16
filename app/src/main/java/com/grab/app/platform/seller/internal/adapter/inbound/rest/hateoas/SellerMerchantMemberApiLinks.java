package com.grab.app.platform.seller.internal.adapter.inbound.rest.hateoas;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.controller.SellerMerchantMemberController;
import org.springframework.hateoas.Link;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public final class SellerMerchantMemberApiLinks {

    private SellerMerchantMemberApiLinks() {
    }

    public static Link listMembers(String merchantId) {
        return linkTo(methodOn(SellerMerchantMemberController.class).listMembers(merchantId))
                .withRel("list-merchant-members");
    }

    public static Link acceptInvitation(String merchantId, String memberId) {
        return linkTo(methodOn(SellerMerchantMemberController.class).acceptInvitation(merchantId, memberId, null))
                .withRel("accept-merchant-member-invitation");
    }

    public static Link updateRole(String merchantId, String memberId) {
        return linkTo(methodOn(SellerMerchantMemberController.class).updateRole(merchantId, memberId, null, null))
                .withRel("update-merchant-member-role");
    }

    public static Link removeMember(String merchantId, String memberId) {
        return linkTo(methodOn(SellerMerchantMemberController.class).removeMember(merchantId, memberId, null))
                .withRel("remove-merchant-member");
    }

    public static Link inviteMember(String merchantId) {
        return linkTo(methodOn(SellerMerchantMemberController.class).inviteMember(merchantId, null, null))
                .withRel("invite-merchant-member");
    }
}

package com.grab.app.platform.seller.internal.command.model;

import com.grab.app.platform.seller.port.outbound.SellerMerchantOutboundPort.MerchantMemberOutboundResult;
import com.grab.app.platform.seller.port.outbound.SellerMerchantOutboundPort.MerchantOutboundResult;
import com.grab.app.platform.seller.port.outbound.SellerMerchantOutboundPort.StorefrontOutboundResult;
import com.grab.framework.cqrs.command.Command;

import java.time.Instant;

public final class SellerMerchantCommands {

    private SellerMerchantCommands() {}

    public record StartSellerMerchantApplicationCommand(
            String actorId,
            String businessType,
            String displayName,
            String primaryMarketCountryCode
    ) implements Command<MerchantOutboundResult> {}

    public record SubmitSellerMerchantApplicationCommand(
            String applicationId,
            String actorId
    ) implements Command<MerchantOutboundResult> {}

    public record UpdateSellerMerchantProfileCommand(
            String merchantId,
            String actorId,
            String legalName,
            String displayName,
            String registrationCountryCode,
            String registrationNumber,
            String contactEmail,
            String contactPhone,
            String addressLine1,
            String addressLine2,
            String addressCity,
            String addressRegion,
            String addressPostalCode,
            String addressCountryCode
    ) implements Command<MerchantOutboundResult> {}

    public record CreateSellerStorefrontCommand(
            String merchantId,
            String name,
            String slug
    ) implements Command<StorefrontOutboundResult> {}

    public record UpdateSellerStorefrontCommand(
            String merchantId,
            String storefrontId,
            String name,
            String slug
    ) implements Command<StorefrontOutboundResult> {}

    public record ActivateSellerStorefrontCommand(
            String merchantId,
            String storefrontId
    ) implements Command<Void> {}

    public record CloseSellerStorefrontCommand(
            String merchantId,
            String storefrontId
    ) implements Command<Void> {}

    public record InviteSellerMerchantMemberCommand(
            String merchantId,
            String actorId,
            String targetUserId,
            String role,
            Instant expiresAt
    ) implements Command<MerchantMemberOutboundResult> {}

    public record AcceptSellerMerchantInvitationCommand(
            String merchantId,
            String memberId,
            String actorId
    ) implements Command<MerchantMemberOutboundResult> {}

    public record UpdateSellerMerchantMemberRoleCommand(
            String merchantId,
            String memberId,
            String actorId,
            String newRole
    ) implements Command<MerchantMemberOutboundResult> {}

    public record RemoveSellerMerchantMemberCommand(
            String merchantId,
            String memberId,
            String actorId
    ) implements Command<Void> {}
}


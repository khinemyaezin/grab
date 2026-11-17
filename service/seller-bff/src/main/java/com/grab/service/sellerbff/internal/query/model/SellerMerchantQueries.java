package com.grab.service.sellerbff.internal.query.model;

import com.grab.service.sellerbff.port.outbound.SellerMerchantOutboundPort.C2CApplicationOutboundResult;
import com.grab.service.sellerbff.port.outbound.SellerMerchantOutboundPort.FirstPartyRetailerApplicationOutboundResult;
import com.grab.service.sellerbff.port.outbound.SellerMerchantOutboundPort.MerchantMemberOutboundResult;
import com.grab.service.sellerbff.port.outbound.SellerMerchantOutboundPort.MerchantOutboundResult;
import com.grab.service.sellerbff.port.outbound.SellerMerchantOutboundPort.StorefrontOutboundResult;
import com.grab.framework.cqrs.query.Query;

import java.util.List;
import java.util.Optional;

public final class SellerMerchantQueries {

    private SellerMerchantQueries() {}

    public record GetSellerMerchantQuery(
            String merchantId,
            String actorId
    ) implements Query<Optional<MerchantOutboundResult>> {}

    public record ListMySellerMerchantsQuery(
            String actorId
    ) implements Query<List<MerchantOutboundResult>> {}

    public record GetSellerStorefrontQuery(
            String merchantId,
            String storefrontId
    ) implements Query<Optional<StorefrontOutboundResult>> {}

    public record ListSellerStorefrontsQuery(
            String merchantId
    ) implements Query<List<StorefrontOutboundResult>> {}

    public record ListSellerMerchantMembersQuery(
            String merchantId
    ) implements Query<List<MerchantMemberOutboundResult>> {}

    public record ListSellerMerchantReviewQueueQuery(
            String status
    ) implements Query<List<MerchantOutboundResult>> {}

    public record GetC2CApplicationQuery(
            String applicantUserId
    ) implements Query<C2CApplicationOutboundResult> {}

    public record GetFirstPartyRetailerApplicationQuery(
            String applicantUserId
    ) implements Query<FirstPartyRetailerApplicationOutboundResult> {}
}


package com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.MerchantMemberResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.hateoas.SellerMerchantMemberApiLinks;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MerchantMemberModelAssembler implements RepresentationModelAssembler<MerchantMemberResponse, EntityModel<MerchantMemberResponse>> {

    @Override
    public EntityModel<MerchantMemberResponse> toModel(MerchantMemberResponse member) {
        if (member == null) {
            return null;
        }
        EntityModel<MerchantMemberResponse> model = EntityModel.of(member);
        if (member.merchantId() != null) {
            model.add(SellerMerchantMemberApiLinks.listMembers(member.merchantId()));
            if ("INVITED".equalsIgnoreCase(member.status()) && member.memberId() != null) {
                model.add(SellerMerchantMemberApiLinks.acceptInvitation(member.merchantId(), member.memberId()));
            }
            if ("ACTIVE".equalsIgnoreCase(member.status()) && member.memberId() != null) {
                model.add(SellerMerchantMemberApiLinks.updateRole(member.merchantId(), member.memberId()));
                model.add(SellerMerchantMemberApiLinks.removeMember(member.merchantId(), member.memberId()));
            }
        }
        return model;
    }

    public CollectionModel<EntityModel<MerchantMemberResponse>> listMembersModel(List<MerchantMemberResponse> responses, String merchantId) {
        List<EntityModel<MerchantMemberResponse>> items = responses.stream()
                .map(this::toModel)
                .toList();
        Link collection = SellerMerchantMemberApiLinks.listMembers(merchantId).expand();
        CollectionModel<EntityModel<MerchantMemberResponse>> model = CollectionModel.of(items, collection.withSelfRel(), collection);
        model.add(SellerMerchantMemberApiLinks.inviteMember(merchantId));
        return model;
    }
}

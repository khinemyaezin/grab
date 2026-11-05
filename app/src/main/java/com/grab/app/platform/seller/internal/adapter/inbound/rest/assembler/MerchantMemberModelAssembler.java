package com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.controller.SellerMerchantMemberController;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.MerchantMemberResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class MerchantMemberModelAssembler implements RepresentationModelAssembler<MerchantMemberResponse, EntityModel<MerchantMemberResponse>> {

    @Override
    public EntityModel<MerchantMemberResponse> toModel(MerchantMemberResponse member) {
        if (member == null) {
            return null;
        }
        EntityModel<MerchantMemberResponse> model = EntityModel.of(member);
        if (member.merchantId() != null) {
            model.add(linkTo(methodOn(SellerMerchantMemberController.class).listMembers(member.merchantId())).withRel("list-merchant-members"));
        }
        return model;
    }
}

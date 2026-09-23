package com.grab.store.merchant.internal.api.rest.assembler;

import com.grab.store.merchant.internal.api.rest.controller.MerchantMemberController;
import com.grab.store.merchant.internal.api.rest.dto.response.MerchantMemberResponse;
import com.grab.store.merchant.internal.config.MerchantEnabled;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.StreamSupport;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
@MerchantEnabled
public class MerchantMemberModelAssembler implements RepresentationModelAssembler<MerchantMemberResponse, EntityModel<MerchantMemberResponse>> {

    @Override
    public EntityModel<MerchantMemberResponse> toModel(MerchantMemberResponse response) {
        EntityModel<MerchantMemberResponse> model = EntityModel.of(
                response,
                linkTo(methodOn(MerchantMemberController.class)
                        .list(response.merchantId()))
                        .withSelfRel()
        );

        if ("INVITED".equalsIgnoreCase(response.status())) {
            model.add(linkTo(methodOn(MerchantMemberController.class)
                    .accept(response.merchantId(), response.memberId(), null))
                    .withRel("accept-invitation"));
        }

        if ("ACTIVE".equalsIgnoreCase(response.status())) {
            model.add(linkTo(methodOn(MerchantMemberController.class)
                    .changeRole(response.merchantId(), response.memberId(), null, null))
                    .withRel("change-role"));
            model.add(linkTo(methodOn(MerchantMemberController.class)
                    .remove(response.merchantId(), response.memberId(), null))
                    .withRel("remove-member"));
        }

        return model;
    }

    public CollectionModel<EntityModel<MerchantMemberResponse>> toCollectionModel(
            Iterable<? extends MerchantMemberResponse> entities, String merchantId
    ) {
        List<EntityModel<MerchantMemberResponse>> models = StreamSupport.stream(entities.spliterator(), false)
                .map(this::toModel)
                .toList();

        return CollectionModel.of(
                models,
                linkTo(methodOn(MerchantMemberController.class).list(merchantId)).withSelfRel()
        );
    }
}

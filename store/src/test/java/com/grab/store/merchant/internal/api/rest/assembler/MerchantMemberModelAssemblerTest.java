package com.grab.store.merchant.internal.api.rest.assembler;

import com.grab.store.merchant.internal.api.rest.dto.response.MerchantMemberResponse;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MerchantMemberModelAssemblerTest {
    private final MerchantMemberModelAssembler assembler = new MerchantMemberModelAssembler();
    private final Instant now = Instant.parse("2026-09-23T10:00:00Z");

    @Test
    void toModel_withInvitedMember_shouldExposeAcceptInvitationLink() {
        EntityModel<MerchantMemberResponse> model = assembler.toModel(response("INVITED"));

        assertThat(model.getLink("self")).isPresent();
        assertThat(model.getLink("accept-invitation")).isPresent();
        assertThat(model.getLink("change-role")).isEmpty();
        assertThat(model.getLink("remove-member")).isEmpty();
    }

    @Test
    void toModel_withActiveMember_shouldExposeRoleAndRemoveLinks() {
        EntityModel<MerchantMemberResponse> model = assembler.toModel(response("ACTIVE"));

        assertThat(model.getLink("self")).isPresent();
        assertThat(model.getLink("accept-invitation")).isEmpty();
        assertThat(model.getLink("change-role")).isPresent();
        assertThat(model.getLink("remove-member")).isPresent();
    }

    @Test
    void toCollectionModel_shouldIncludeSelfLink() {
        CollectionModel<EntityModel<MerchantMemberResponse>> collection =
                assembler.toCollectionModel(List.of(response("ACTIVE")), "mer-1");

        assertThat(collection.getLink("self")).isPresent();
        assertThat(collection.getContent()).hasSize(1);
    }

    private MerchantMemberResponse response(String status) {
        return new MerchantMemberResponse(
                "mem-1",
                "mer-1",
                "usr-1",
                "OPERATOR",
                status,
                null,
                null,
                now,
                now,
                now,
                0L
        );
    }
}

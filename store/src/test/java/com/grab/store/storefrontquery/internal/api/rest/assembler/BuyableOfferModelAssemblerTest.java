package com.grab.store.storefrontquery.internal.api.rest.assembler;

import com.grab.store.storefrontquery.internal.api.rest.dto.response.BuyableOfferResponse;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.EntityModel;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BuyableOfferModelAssemblerTest {

    private final BuyableOfferModelAssembler assembler = new BuyableOfferModelAssembler();

    @Test
    void toModel_validResponse_addsSelfAndListLinks() {
        BuyableOfferResponse response = new BuyableOfferResponse(
                "web-1",
                "prod-1",
                "seller-1",
                "category-1",
                "Iced Coffee",
                "iced-coffee",
                null,
                null,
                "ACTIVE",
                true,
                true,
                true,
                List.of(),
                List.of()
        );

        EntityModel<BuyableOfferResponse> model = assembler.toModel(response);

        assertThat(model.getContent()).isEqualTo(response);
        assertThat(model.getLink("self")).isPresent();
        assertThat(model.getLink("list-storefront-products")).isPresent();
    }
}

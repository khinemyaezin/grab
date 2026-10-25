package com.grab.app.channeldiscovery.storefrontquery.internal.api.rest.assembler;

import com.grab.app.channeldiscovery.storefrontquery.internal.api.rest.dto.response.BuyableOfferResponse;
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
                List.of(),
                "region-1",
                "SG",
                "SGD",
                "en-SG",
                true,
                true,
                "10.00",
                "12.00"
        );

        EntityModel<BuyableOfferResponse> model = assembler.toModel(response);

        assertThat(model.getContent()).isEqualTo(response);
        assertThat(model.getLink("self")).isPresent();
        assertThat(model.getLink("list-storefront-products")).isPresent();
    }

    @Test
    void toModel_contextualResponseUsesHeaderBasedLinks() {
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
                List.of(),
                "region-1",
                "SG",
                "SGD",
                "en-SG",
                true,
                true,
                "10.00",
                "12.00"
        );

        EntityModel<BuyableOfferResponse> model = assembler.toModel(response);

        assertThat(model.getRequiredLink("self").getHref())
                .contains("/api/v1/storefront-query/products/iced-coffee")
                .doesNotContain("salesChannelId");
    }
}

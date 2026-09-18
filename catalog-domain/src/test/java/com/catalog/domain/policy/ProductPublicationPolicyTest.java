package com.catalog.domain.policy;

import com.catalog.domain.aggregate.Product;
import com.catalog.domain.exception.CatalogDomainValidationException;
import com.catalog.domain.valueobject.ProductStatus;
import com.grab.framework.id.Id;
import com.grab.framework.id.impl.CommonId;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductPublicationPolicyTest {

    private final ProductPublicationPolicy policy = new ProductPublicationPolicy();

    @Test
    void requirePublishable_allowsActiveProduct() {
        assertThatCode(() -> policy.requirePublishable(product(ProductStatus.ACTIVE)))
                .doesNotThrowAnyException();
    }

    @Test
    void requirePublishable_rejectsNonActiveProduct() {
        assertThatThrownBy(() -> policy.requirePublishable(product(ProductStatus.DRAFT)))
                .isInstanceOf(CatalogDomainValidationException.class)
                .hasMessageContaining("ACTIVE");
    }

    private static Product product(ProductStatus status) {
        return new Product(
                id("product-1"),
                id("merchant-1"),
                "Shirt",
                id("cat-1"),
                null,
                status,
                null,
                null,
                null,
                List.of()
        );
    }

    private static Id id(String value) {
        return new CommonId(value);
    }
}

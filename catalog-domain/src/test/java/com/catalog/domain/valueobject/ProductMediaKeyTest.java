package com.catalog.domain.valueobject;

import com.grab.framework.id.impl.CommonId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductMediaKeyTest {

    @Test
    void factoryMintsStagedAndProductKeysWithoutExtraDot() {
        ProductMediaKey.Factory keys = new ProductMediaKey.Factory(new CommonId("merchant-1"));

        assertThat(keys.staged(new CommonId("object-1"), ".jpg").value())
                .isEqualTo("merchants/merchant-1/staged/object-1.jpg");
        assertThat(keys.forProduct(new CommonId("product-1"), new CommonId("object-1"), ".jpg").value())
                .isEqualTo("merchants/merchant-1/products/product-1/object-1.jpg");
    }

    @Test
    void stagedKeyIsOwnedAndNotForeign() {
        ProductMediaKey key = new ProductMediaKey("merchants/merchant-1/staged/object-1.jpg");

        assertThat(key.isStaged()).isTrue();
        assertThat(key.isOwnedBy(new CommonId("merchant-1"))).isTrue();
        assertThat(key.isForeignStaged(new CommonId("merchant-1"))).isFalse();
        assertThat(key.isForeignStaged(new CommonId("other-merchant"))).isTrue();
        assertThat(key.fileName()).isEqualTo("object-1.jpg");
    }

    @Test
    void promoteToProductMovesFilenameUnderProductPrefix() {
        ProductMediaKey staged = new ProductMediaKey("merchants/merchant-1/staged/object-1.jpg");

        ProductMediaKey product = staged.promoteToProduct(new CommonId("product-1"));

        assertThat(product.value()).isEqualTo("merchants/merchant-1/products/product-1/object-1.jpg");
        assertThat(product.isStaged()).isFalse();
        assertThat(product.isOwnedBy(new CommonId("merchant-1"))).isTrue();
    }

    @Test
    void promoteToProductRejectsNonStagedKeys() {
        ProductMediaKey product = new ProductMediaKey("merchants/merchant-1/products/product-1/keep.jpg");

        assertThatThrownBy(() -> product.promoteToProduct(new CommonId("product-1")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("storageKey is not staged");
    }

    @Test
    void legacyImagePathIsNotStagedOrOwned() {
        ProductMediaKey key = new ProductMediaKey("/images/original.png");

        assertThat(key.isStaged()).isFalse();
        assertThat(key.isOwnedBy(new CommonId("merchant-1"))).isFalse();
        assertThat(key.isForeignStaged(new CommonId("merchant-1"))).isFalse();
    }

    @Test
    void rejectsBlankAndPathTraversal() {
        assertThatThrownBy(() -> new ProductMediaKey(" "))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ProductMediaKey("merchants/m/staged/../secret.jpg"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("storageKey must not contain path traversal");
    }
}

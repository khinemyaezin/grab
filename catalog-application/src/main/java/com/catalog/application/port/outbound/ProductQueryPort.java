package com.catalog.application.port.outbound;

import com.catalog.application.model.read.ProductDetailView;
import com.catalog.application.model.read.ProductHeroMediaView;
import com.catalog.application.model.read.ProductPublicationView;
import com.catalog.application.model.read.ProductSearchCriteria;
import com.catalog.application.model.read.ProductVariantRefView;
import com.catalog.application.model.read.ProductView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProductQueryPort {
    Page<ProductView> search(ProductSearchCriteria criteria, Pageable pageable);

    List<ProductHeroMediaView> findHeroMediasByProductIds(Collection<String> productIds);

    List<ProductVariantRefView> findActiveVariantsByProductIds(Collection<String> productIds);

    List<ProductPublicationView> findPublicationsByProductIds(Collection<String> productIds);

    Optional<ProductView> findByIdAndMerchantId(String productId, String merchantId);

    Optional<ProductDetailView> findDetailByIdAndMerchantId(String productId, String merchantId);

    Optional<ProductDetailView> findDetailBySlug(String slug);

    Optional<ProductDetailView.VariantView> findVariantDetail(
            String productId,
            String merchantId,
            String variantId
    );
}

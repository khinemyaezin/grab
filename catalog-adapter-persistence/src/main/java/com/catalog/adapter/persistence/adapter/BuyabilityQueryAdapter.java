package com.catalog.adapter.persistence.adapter;

import com.catalog.application.port.outbound.BuyabilityQueryPort;
import com.catalog.domain.port.outbound.ProductPublicationRepository;
import com.catalog.adapter.persistence.entity.ProductEntity;
import com.catalog.adapter.persistence.entity.ProductPublicationEntity;
import com.catalog.adapter.persistence.entity.ProductVariantEntity;
import com.catalog.adapter.persistence.repository.ProductPublicationJpaRepository;
import com.catalog.adapter.persistence.repository.ProductVariantJpaRepo;
import com.grab.framework.id.IdGenerator;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class BuyabilityQueryAdapter implements BuyabilityQueryPort {

    private final ProductVariantJpaRepo productVariantJpaRepo;
    private final ProductPublicationRepository productPublicationRepository;
    private final ProductPublicationJpaRepository productPublicationJpaRepository;
    private final IdGenerator idGenerator;

    @Override
    public Optional<VariantSlice> findVariant(String variantId) {
        return productVariantJpaRepo.findByUuid(variantId).map(this::toSlice);
    }

    @Override
    public boolean isPublished(String variantId, String salesChannelId) {
        return productPublicationRepository.exists(
                idGenerator.convertIdFrom(variantId),
                idGenerator.convertIdFrom(salesChannelId)
        );
    }

    @Override
    public List<String> variantIdsForProduct(String productId) {
        return productVariantJpaRepo.findByProduct_Uuid(productId).stream()
                .map(ProductVariantEntity::getUuid)
                .toList();
    }

    @Override
    public List<PublicationSlice> listPublications() {
        return productPublicationJpaRepository.findAll().stream()
                .map(this::toPublication)
                .flatMap(Optional::stream)
                .toList();
    }

    @Override
    public List<String> salesChannelIdsForVariant(String variantId) {
        return productPublicationRepository.findByVariantId(idGenerator.convertIdFrom(variantId)).stream()
                .map(publication -> publication.getSalesChannelId().getValue())
                .toList();
    }

    private Optional<PublicationSlice> toPublication(ProductPublicationEntity entity) {
        return productVariantJpaRepo.findById(entity.getVariantId())
                .map(variant -> new PublicationSlice(variant.getUuid(), entity.getSalesChannelId()));
    }

    private VariantSlice toSlice(ProductVariantEntity variant) {
        ProductEntity product = variant.getProduct();
        return new VariantSlice(
                variant.getUuid(),
                product == null ? null : product.getUuid(),
                variant.getMerchantId(),
                variant.getSku(),
                product == null ? null : product.getName(),
                product == null ? null : product.getSlug(),
                product == null || product.getStatus() == null ? null : product.getStatus().name(),
                !variant.isManageInventory(),
                variant.getThumbnailMediaUuid()
        );
    }
}

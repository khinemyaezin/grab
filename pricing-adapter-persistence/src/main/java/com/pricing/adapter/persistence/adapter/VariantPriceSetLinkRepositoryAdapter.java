package com.pricing.adapter.persistence.adapter;

import com.grab.framework.support.PersistenceExecutor;
import com.pricing.adapter.persistence.entity.VariantPriceSetLinkEntity;
import com.pricing.adapter.persistence.mapper.jpa.VariantPriceSetLinkJpaAssembler;
import com.pricing.adapter.persistence.repository.jpa.VariantPriceSetLinkJpaRepository;
import com.pricing.application.model.read.VariantPriceSetLinkView;
import com.pricing.domain.port.outbound.VariantPriceSetLinkRepository;
import com.pricing.domain.valueobject.VariantPriceSetLink;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class VariantPriceSetLinkRepositoryAdapter implements VariantPriceSetLinkRepository {

    private final VariantPriceSetLinkJpaRepository jpaRepository;
    private final VariantPriceSetLinkJpaAssembler assembler;
    private final PersistenceExecutor executor;

    @Override
    public Optional<VariantPriceSetLink> findByVariantId(String variantId) {
        return executor.query("VariantPriceSetLink", () ->
                jpaRepository.findById(variantId).map(entity -> toDomain(assembler.toView(entity))));
    }

    @Override
    public void save(VariantPriceSetLink link) {
        VariantPriceSetLinkView view = toView(link);
        executor.command("VariantPriceSetLink", () -> {
            Optional<VariantPriceSetLinkEntity> linkEntity = jpaRepository.findById(link.variantId());
            VariantPriceSetLinkEntity entity;

            if (linkEntity.isPresent()) {
                entity = assembler.buildEntity(view, linkEntity.get());
            } else {
                entity = assembler.buildEntity(view, null);
            }

            jpaRepository.save(entity);
        });
    }

    private static VariantPriceSetLink toDomain(VariantPriceSetLinkView view) {
        return new VariantPriceSetLink(
                view.variantId(),
                view.priceSetId(),
                view.productId(),
                view.sku(),
                view.merchantId(),
                view.createdAt(),
                view.updatedAt()
        );
    }

    private static VariantPriceSetLinkView toView(VariantPriceSetLink link) {
        return new VariantPriceSetLinkView(
                link.variantId(),
                link.priceSetId(),
                link.productId(),
                link.sku(),
                link.merchantId(),
                link.createdAt(),
                link.updatedAt()
        );
    }

    @Override
    public void deleteByVariantId(String variantId) {
        executor.command("VariantPriceSetLink", () -> jpaRepository.deleteById(variantId));
    }

    @Override
    public void deleteByPriceSetId(String priceSetId) {
        executor.command("VariantPriceSetLink", () -> jpaRepository.deleteByPriceSetId(priceSetId));
    }
}

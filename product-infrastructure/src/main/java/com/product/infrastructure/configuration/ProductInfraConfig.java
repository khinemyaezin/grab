package com.product.infrastructure.configuration;

import com.grab.framework.id.IdGenerator;
import com.grab.framework.mapper.CommonMapper;
import com.product.domain.repository.ProductRepository;
import com.product.infrastructure.entity.product.factory.ProductEntityFactory;
import com.product.infrastructure.entity.product.factory.ProductVariantEntityFactory;
import com.product.infrastructure.entity.product.factory.ProductVariantOptionEntityFactory;
import com.product.infrastructure.event.ApplicationDomainEventProducer;
import com.product.infrastructure.event.DomainEventProducer;
import com.product.infrastructure.mapper.product.ProductEntityMapper;
import com.product.infrastructure.mapper.product.ProductMapper;
import com.product.infrastructure.mapper.product.ProductVariantEntityMapper;
import com.product.infrastructure.mapper.product.ProductVariantMapper;
import com.product.infrastructure.mapper.product.ProductVariationMapper;
import com.product.infrastructure.repository.fascade.ProductFacadeRepository;
import com.product.infrastructure.repository.jpa.ProductJpaRepository;
import com.product.infrastructure.repository.jpa.ProductVariantJpaRepository;
import com.product.infrastructure.repository.jpa.VariantOptionJpaRepository;
import com.product.infrastructure.service.ProductService;
import com.product.infrastructure.service.ProductVariantOptionService;
import com.product.infrastructure.service.ProductVariantService;
import com.product.infrastructure.service.impl.ProductServiceImpl;
import com.product.infrastructure.service.impl.ProductVariantOptionServiceImpl;
import com.product.infrastructure.service.impl.ProductVariantServiceImpl;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(DomainConfig.class)
public class ProductInfraConfig {
    @Bean
    public CommonMapper commonMapper() {
        return new CommonMapper();
    }
    @Bean
    public ProductVariationMapper productVariationMapper(IdGenerator idGenerator) {
        return new ProductVariationMapper(idGenerator);
    }

    @Bean
    public ProductVariantMapper productVariantMapper(IdGenerator idGenerator,
                                                     ProductVariationMapper productVariationMapper) {
        return new ProductVariantMapper(idGenerator, productVariationMapper);
    }

    @Bean
    public ProductMapper productMapper(IdGenerator idGenerator,
                                       ProductVariantMapper productVariantMapper) {
        return new ProductMapper(idGenerator, productVariantMapper);
    }

    @Bean
    public ProductEntityFactory productEntityFactory(ProductEntityMapper productEntityMapper) {
        return new ProductEntityFactory(productEntityMapper);
    }

    @Bean
    public ProductVariantEntityFactory productVariantEntityFactory(
            ProductVariantEntityMapper productVariantEntityMapper) {
        return new ProductVariantEntityFactory(productVariantEntityMapper);
    }

    @Bean
    public ProductVariantOptionEntityFactory productVariantOptionEntityFactory(IdGenerator idGenerator) {
        return new ProductVariantOptionEntityFactory(idGenerator);
    }

    @Bean
    public ProductVariantOptionService productVariantOptionService(
            ProductVariantOptionEntityFactory productVariantOptionEntityFactory,
            ProductVariationMapper productVariationMapper,
            VariantOptionJpaRepository variantOptionJpaRepository) {
        return new ProductVariantOptionServiceImpl(
                productVariantOptionEntityFactory,
                productVariationMapper,
                variantOptionJpaRepository);
    }

    @Bean
    public ProductVariantService productVariantService(
            ProductVariantEntityFactory productVariantEntityFactory,
            ProductVariantOptionService productVariantOptionService,
            ProductVariantJpaRepository productVariantJpaRepository,
            ProductVariantEntityMapper productVariantEntityMapper) {
        return new ProductVariantServiceImpl(
                productVariantEntityFactory,
                productVariantOptionService,
                productVariantJpaRepository,
                productVariantEntityMapper);
    }

    @Bean
    public ProductService productService(
            ProductJpaRepository productJpaRepository,
            ProductEntityMapper productEntityMapper,
            ProductEntityFactory productEntityFactory) {
        return new ProductServiceImpl(
                productJpaRepository,
                productEntityMapper,
                productEntityFactory);
    }

    @Bean
    public DomainEventProducer domainEventProducer(ApplicationEventPublisher applicationEventPublisher) {
        return new ApplicationDomainEventProducer(applicationEventPublisher);
    }

    @Bean
    public ProductRepository productRepository(
            ProductService productService,
            ProductVariantService productVariantService,
            DomainEventProducer domainEventProducer,
            ProductMapper productMapper) {
        return new ProductFacadeRepository(
                productService,
                productVariantService,
                domainEventProducer,
                productMapper);
    }
}
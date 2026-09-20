#!/usr/bin/env python3
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SERVICE = ROOT / "catalog-application/src/main/java/com/catalog/application/service"
OUT = ROOT / "store/src/main/java/com/grab/store/catalog/internal/config/CatalogUseCaseConfig.java"

DOMAIN_BEANS = '''    @Bean
    public SkuGenerator skuGenerator() {
        return new DefaultSkuGenerator();
    }

    @Bean
    public MatrixCombinationService variantCombination() {
        return new DefaultMatrixCombinationService();
    }

    @Bean
    public VariantDeletionStrategy variantDeletionStrategy() {
        return new FullOptionHardDeleteStrategy();
    }

    @Bean
    public Comparator<ProductVariation> getVariationComparator() {
        return new ProductVariationComparator();
    }

    @Bean
    public MatrixKeyGenerator variationKeyGenerator(Comparator<ProductVariation> getVariationComparator) {
        return new DefaultMatrixKeyGenerator(getVariationComparator);
    }

    @Bean
    public VariationMatrixMatcher variationMatrixMatcher(MatrixKeyGenerator matrixKeyGenerator) {
        return new DefaultVariationMatrixMatcher(matrixKeyGenerator);
    }

    @Bean
    public MatrixCombinationSynchronizer variantCombinationManager(
            MatrixKeyGenerator matrixKeyGenerator,
            VariationMatrixMatcher variationMatrixMatcher) {
        return new DefaultMatrixCombinationSynchronizer(matrixKeyGenerator, variationMatrixMatcher);
    }

    @Bean
    public ProductMediaService productMediaService() {
        return new DefaultProductMediaService();
    }

    @Bean
    public UniqueSlugResolver uniqueSlugResolver(ProductRepository productRepository) {
        return new UniqueSlugResolver(productRepository);
    }

    @Bean
    public MediaUploadValidator mediaUploadValidator(
            @Value("${storage.upload.max-size-bytes:10485760}") long maxSizeBytes) {
        return new MediaUploadValidator(maxSizeBytes);
    }

    @Bean
    public ProductMediaQueryMapper productMediaQueryMapper(FileStoragePort fileStoragePort) {
        return new ProductMediaQueryMapper(fileStoragePort);
    }
'''

def parse_service(path: Path):
    text = path.read_text(encoding="utf-8")
    m = re.search(r"public class (\w+) implements (\w+)UseCase", text)
    if not m:
        return None
    service, usecase = m.group(1), m.group(2)
    fields = re.findall(r"private final (\S+) (\w+);", text)
    return usecase, service, fields


def bean_method(usecase: str, service: str, fields: list[tuple[str, str]]) -> str:
    params = ", ".join(f"{t} {n}" for t, n in fields)
    args = ", ".join(n for _, n in fields)
    method = usecase[0].lower() + usecase[1:] if usecase else usecase
    return f"""
    @Bean
    public {usecase}UseCase {method}UseCase({params}) {{
        return new {service}({args});
    }}"""


beans = []
for path in sorted(SERVICE.glob("*Service.java")):
    parsed = parse_service(path)
    if parsed:
        beans.append(bean_method(*parsed))

header = """package com.grab.store.catalog.internal.config;

import com.catalog.application.port.inbound.*;
import com.catalog.application.query.ProductMediaQueryMapper;
import com.catalog.application.service.*;
import com.catalog.domain.port.outbound.*;
import com.catalog.domain.service.*;
import com.catalog.domain.service.impl.*;
import com.catalog.domain.valueobject.ProductVariation;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.storage.FileStoragePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Comparator;

@Configuration
public class CatalogUseCaseConfig {
"""

footer = "\n}\n"

OUT.write_text(header + DOMAIN_BEANS + "".join(beans) + footer, encoding="utf-8")
print("Wrote", OUT, "beans:", len(beans))

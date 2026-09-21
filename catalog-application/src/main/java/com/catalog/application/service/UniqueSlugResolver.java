package com.catalog.application.service;

import com.catalog.domain.port.outbound.ProductRepository;
import com.grab.framework.id.Id;
import com.catalog.application.exception.CatalogServiceError;
import com.catalog.application.exception.CatalogServiceException;
import lombok.RequiredArgsConstructor;

import java.util.Locale;

@RequiredArgsConstructor
public class UniqueSlugResolver {

    private final ProductRepository productRepository;

    public String resolve(Id merchantId, String requestedSlug, String name, String currentProductId) {
        String baseSlug = normalize(requestedSlug == null || requestedSlug.isBlank() ? name : requestedSlug);
        if (baseSlug == null || baseSlug.isBlank()) {
            throw new CatalogServiceException(new CatalogServiceError.SlugBlank());
        }

        if (!productRepository.isSlugTaken(merchantId, baseSlug, currentProductId)) {
            return baseSlug;
        }

        int suffix = 2;
        String nextSlug = baseSlug + "-" + suffix;
        while (productRepository.isSlugTaken(merchantId, nextSlug, currentProductId)) {
            suffix++;
            nextSlug = baseSlug + "-" + suffix;
        }
        
        return nextSlug;
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String slug = value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-");
        return slug.replaceAll("(^-+)|(-+$)", "");
    }
}

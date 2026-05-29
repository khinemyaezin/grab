package com.grab.store.catalog.internal.util;

import com.catalog.domain.repository.ProductRepository;
import com.grab.store.catalog.internal.exception.CatalogServiceError;
import com.grab.store.catalog.internal.exception.CatalogServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UniqueSlugResolver {

    private final ProductRepository productRepository;

    public String resolve(String requestedSlug, String name, String currentProductId) {
        String baseSlug = normalize(requestedSlug == null || requestedSlug.isBlank() ? name : requestedSlug);
        if (baseSlug == null || baseSlug.isBlank()) {
            throw new CatalogServiceException(new CatalogServiceError.SlugBlank());
        }

        if (!productRepository.isSlugTaken(baseSlug, currentProductId)) {
            return baseSlug;
        }

        int suffix = 2;
        String nextSlug = baseSlug + "-" + suffix;
        while (productRepository.isSlugTaken(nextSlug, currentProductId)) {
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

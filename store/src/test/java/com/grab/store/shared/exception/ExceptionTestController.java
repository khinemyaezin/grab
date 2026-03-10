package com.grab.store.shared.exception;

import com.inventory.domain.exception.InventoryDomainError;
import com.inventory.domain.exception.InventoryDomainValidationException;
import com.inventory.infrastructure.exception.InventoryInfraError;
import com.inventory.infrastructure.exception.InventoryInfraException;
import com.grab.store.inventory.internal.exception.InventoryServiceError;
import com.grab.store.inventory.internal.exception.InventoryServiceException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
class ExceptionTestController {

    @GetMapping("/test/errors/service-not-found")
    void serviceNotFound() {
        throw new InventoryServiceException(new InventoryServiceError.LocationNotFound("loc-1"));
    }

    @GetMapping("/test/errors/domain-insufficient")
    void domainInsufficient() {
        throw new InventoryDomainValidationException(
                new InventoryDomainError.InsufficientQuantity(3, 9),
                "Insufficient quantity."
        );
    }

    @GetMapping("/test/errors/infra-internal")
    void infraInternal() {
        throw new InventoryInfraException(
                new InventoryInfraError.PersistenceInternal("InventoryItem", "db down"),
                "Infrastructure failure."
        );
    }

    @GetMapping("/test/errors/unexpected")
    void unexpected() {
        throw new RuntimeException("boom");
    }

    @PostMapping("/test/errors/validation")
    void validation(@Valid @RequestBody ValidationPayload payload) {
        // no-op
    }

    @GetMapping("/test/errors/constraint")
    void constraint(@RequestParam("page") @Min(1) int page) {
        // no-op
    }

    private record ValidationPayload(@NotBlank String name) {
    }
}

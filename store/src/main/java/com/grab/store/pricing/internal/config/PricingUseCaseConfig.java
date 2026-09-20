package com.grab.store.pricing.internal.config;

import com.grab.framework.id.IdGenerator;
import com.pricing.application.port.inbound.*;
import com.pricing.application.port.outbound.PriceQueryPort;
import com.pricing.application.port.outbound.VariantPriceSetLinkQueryPort;
import com.pricing.application.service.*;
import com.pricing.domain.policy.CalculatePricesPolicy;
import com.pricing.domain.port.outbound.PriceListRepository;
import com.pricing.domain.port.outbound.PricePreferenceRepository;
import com.pricing.domain.port.outbound.PriceSetRepository;
import com.pricing.domain.port.outbound.VariantPriceSetLinkRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PricingUseCaseConfig {

    @Bean
    public CalculatePricesPolicy calculatePricesPolicy() {
        return new CalculatePricesPolicy();
    }

    @Bean
    public AddPriceToPriceListUseCase addPriceToPriceListUseCase(
            PriceListRepository priceListRepository,
            PriceSetRepository priceSetRepository,
            IdGenerator idGenerator
    ) {
        return new AddPriceToPriceListService(priceListRepository, priceSetRepository, idGenerator);
    }

    @Bean
    public AddPriceToPriceSetUseCase addPriceToPriceSetUseCase(
            PriceSetRepository priceSetRepository,
            IdGenerator idGenerator
    ) {
        return new AddPriceToPriceSetService(priceSetRepository, idGenerator);
    }

    @Bean
    public CalculatePricesUseCase calculatePricesUseCase(
            PriceQueryPort priceQueryPort,
            CalculatePricesPolicy calculatePricesPolicy
    ) {
        return new CalculatePricesService(priceQueryPort, calculatePricesPolicy);
    }

    @Bean
    public CreatePriceListUseCase createPriceListUseCase(
            PriceListRepository priceListRepository,
            IdGenerator idGenerator
    ) {
        return new CreatePriceListService(priceListRepository, idGenerator);
    }

    @Bean
    public CreatePricePreferenceUseCase createPricePreferenceUseCase(
            PricePreferenceRepository pricePreferenceRepository,
            IdGenerator idGenerator
    ) {
        return new CreatePricePreferenceService(pricePreferenceRepository, idGenerator);
    }

    @Bean
    public CreatePriceSetUseCase createPriceSetUseCase(
            PriceSetRepository priceSetRepository,
            IdGenerator idGenerator
    ) {
        return new CreatePriceSetService(priceSetRepository, idGenerator);
    }

    @Bean
    public CreateVariantPriceAssignmentUseCase createVariantPriceAssignmentUseCase(
            PriceSetRepository priceSetRepository,
            VariantPriceSetLinkRepository variantPriceSetLinkRepository,
            IdGenerator idGenerator
    ) {
        return new CreateVariantPriceAssignmentService(priceSetRepository, variantPriceSetLinkRepository, idGenerator);
    }

    @Bean
    public DeletePriceListUseCase deletePriceListUseCase(PriceListRepository priceListRepository) {
        return new DeletePriceListService(priceListRepository);
    }

    @Bean
    public DeletePricePreferenceUseCase deletePricePreferenceUseCase(
            PricePreferenceRepository pricePreferenceRepository
    ) {
        return new DeletePricePreferenceService(pricePreferenceRepository);
    }

    @Bean
    public DeletePriceSetUseCase deletePriceSetUseCase(
            PriceSetRepository priceSetRepository,
            VariantPriceSetLinkRepository variantPriceSetLinkRepository
    ) {
        return new DeletePriceSetService(priceSetRepository, variantPriceSetLinkRepository);
    }

    @Bean
    public DeletePriceSetForDeletedVariantUseCase deletePriceSetForDeletedVariantUseCase(
            VariantPriceSetLinkQueryPort variantPriceSetLinkQueryPort,
            VariantPriceSetLinkRepository variantPriceSetLinkRepository,
            PriceSetRepository priceSetRepository,
            IdGenerator idGenerator
    ) {
        return new DeletePriceSetForDeletedVariantService(
                variantPriceSetLinkQueryPort,
                variantPriceSetLinkRepository,
                priceSetRepository,
                idGenerator
        );
    }

    @Bean
    public GetPriceListUseCase getPriceListUseCase(PriceQueryPort priceQueryPort) {
        return new GetPriceListService(priceQueryPort);
    }

    @Bean
    public GetPricePreferenceUseCase getPricePreferenceUseCase(PriceQueryPort priceQueryPort) {
        return new GetPricePreferenceService(priceQueryPort);
    }

    @Bean
    public GetPriceSetUseCase getPriceSetUseCase(PriceQueryPort priceQueryPort) {
        return new GetPriceSetService(priceQueryPort);
    }

    @Bean
    public ListPriceListsUseCase listPriceListsUseCase(PriceQueryPort priceQueryPort) {
        return new ListPriceListsService(priceQueryPort);
    }

    @Bean
    public ListPricePreferencesUseCase listPricePreferencesUseCase(PriceQueryPort priceQueryPort) {
        return new ListPricePreferencesService(priceQueryPort);
    }

    @Bean
    public ListVariantPriceSetLinksUseCase listVariantPriceSetLinksUseCase(
            VariantPriceSetLinkQueryPort variantPriceSetLinkQueryPort
    ) {
        return new ListVariantPriceSetLinksService(variantPriceSetLinkQueryPort);
    }

    @Bean
    public RemovePriceFromPriceListUseCase removePriceFromPriceListUseCase(
            PriceListRepository priceListRepository
    ) {
        return new RemovePriceFromPriceListService(priceListRepository);
    }

    @Bean
    public RemovePriceFromPriceSetUseCase removePriceFromPriceSetUseCase(PriceSetRepository priceSetRepository) {
        return new RemovePriceFromPriceSetService(priceSetRepository);
    }

    @Bean
    public ReplacePriceListRulesUseCase replacePriceListRulesUseCase(
            PriceListRepository priceListRepository,
            IdGenerator idGenerator
    ) {
        return new ReplacePriceListRulesService(priceListRepository, idGenerator);
    }

    @Bean
    public UpdatePriceListUseCase updatePriceListUseCase(PriceListRepository priceListRepository) {
        return new UpdatePriceListService(priceListRepository);
    }

    @Bean
    public UpdatePriceOnPriceSetUseCase updatePriceOnPriceSetUseCase(
            PriceSetRepository priceSetRepository,
            IdGenerator idGenerator
    ) {
        return new UpdatePriceOnPriceSetService(priceSetRepository, idGenerator);
    }

    @Bean
    public UpdatePricePreferenceUseCase updatePricePreferenceUseCase(
            PricePreferenceRepository pricePreferenceRepository
    ) {
        return new UpdatePricePreferenceService(pricePreferenceRepository);
    }

    @Bean
    public UpdateVariantPriceUseCase updateVariantPriceUseCase(
            PriceSetRepository priceSetRepository,
            VariantPriceSetLinkRepository variantPriceSetLinkRepository,
            IdGenerator idGenerator
    ) {
        return new UpdateVariantPriceService(priceSetRepository, variantPriceSetLinkRepository, idGenerator);
    }
}

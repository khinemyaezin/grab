package com.grab.app.merchandising.pricing.internal.config;

import com.grab.framework.id.IdGenerator;
import com.pricing.application.port.inbound.AddPriceToPriceListUseCase;
import com.pricing.application.port.inbound.AddPriceToPriceSetUseCase;
import com.pricing.application.port.inbound.CalculatePricesUseCase;
import com.pricing.application.port.inbound.CreatePriceListUseCase;
import com.pricing.application.port.inbound.CreatePricePreferenceUseCase;
import com.pricing.application.port.inbound.CreatePriceSetUseCase;
import com.pricing.application.port.inbound.CreateVariantPriceAssignmentUseCase;
import com.pricing.application.port.inbound.DeletePriceListUseCase;
import com.pricing.application.port.inbound.DeletePricePreferenceUseCase;
import com.pricing.application.port.inbound.DeletePriceSetForDeletedVariantUseCase;
import com.pricing.application.port.inbound.DeletePriceSetUseCase;
import com.pricing.application.port.inbound.GetPriceListUseCase;
import com.pricing.application.port.inbound.GetPricePreferenceUseCase;
import com.pricing.application.port.inbound.GetPriceSetUseCase;
import com.pricing.application.port.inbound.ListPriceListsUseCase;
import com.pricing.application.port.inbound.ListPricePreferencesUseCase;
import com.pricing.application.port.inbound.ListVariantIdsForPriceSetUseCase;
import com.pricing.application.port.inbound.ListVariantPriceSetLinksUseCase;
import com.pricing.application.port.inbound.QuoteVariantPriceUseCase;
import com.pricing.application.port.inbound.QuoteVariantPricesUseCase;
import com.pricing.application.port.inbound.RemovePriceFromPriceListUseCase;
import com.pricing.application.port.inbound.RemovePriceFromPriceSetUseCase;
import com.pricing.application.port.inbound.ReplacePriceListRulesUseCase;
import com.pricing.application.port.inbound.UpdatePriceListUseCase;
import com.pricing.application.port.inbound.UpdatePriceOnPriceSetUseCase;
import com.pricing.application.port.inbound.UpdatePricePreferenceUseCase;
import com.pricing.application.port.inbound.UpdateVariantPriceUseCase;
import com.pricing.application.port.outbound.PriceQueryPort;
import com.pricing.application.port.outbound.VariantPriceSetLinkQueryPort;
import com.pricing.application.service.AddPriceToPriceListService;
import com.pricing.application.service.AddPriceToPriceSetService;
import com.pricing.application.service.CalculatePricesService;
import com.pricing.application.service.CreatePriceListService;
import com.pricing.application.service.CreatePricePreferenceService;
import com.pricing.application.service.CreatePriceSetService;
import com.pricing.application.service.CreateVariantPriceAssignmentService;
import com.pricing.application.service.DeletePriceListService;
import com.pricing.application.service.DeletePricePreferenceService;
import com.pricing.application.service.DeletePriceSetForDeletedVariantService;
import com.pricing.application.service.DeletePriceSetService;
import com.pricing.application.service.GetPriceListService;
import com.pricing.application.service.GetPricePreferenceService;
import com.pricing.application.service.GetPriceSetService;
import com.pricing.application.service.ListPriceListsService;
import com.pricing.application.service.ListPricePreferencesService;
import com.pricing.application.service.ListVariantIdsForPriceSetService;
import com.pricing.application.service.ListVariantPriceSetLinksService;
import com.pricing.application.service.QuoteVariantPriceService;
import com.pricing.application.service.QuoteVariantPricesService;
import com.pricing.application.service.RemovePriceFromPriceListService;
import com.pricing.application.service.RemovePriceFromPriceSetService;
import com.pricing.application.service.ReplacePriceListRulesService;
import com.pricing.application.service.UpdatePriceListService;
import com.pricing.application.service.UpdatePriceOnPriceSetService;
import com.pricing.application.service.UpdatePricePreferenceService;
import com.pricing.application.service.UpdateVariantPriceService;
import com.pricing.domain.policy.CalculatePricesPolicy;
import com.pricing.domain.policy.QuoteVariantPricesPolicy;
import com.pricing.domain.port.outbound.PriceListRepository;
import com.pricing.domain.port.outbound.PricePreferenceRepository;
import com.pricing.domain.port.outbound.PriceSetRepository;
import com.pricing.domain.port.outbound.VariantPriceSetLinkRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class PricingUseCaseConfig {

    @Bean
    public QuoteVariantPricesPolicy quoteVariantPricesPolicy() {
        return new QuoteVariantPricesPolicy();
    }

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

    @Bean
    public QuoteVariantPriceUseCase quoteVariantPriceUseCase(
            VariantPriceSetLinkQueryPort variantPriceSetLinkQueryPort,
            CalculatePricesUseCase calculatePricesUseCase,
            IdGenerator idGenerator
    ) {
        return new QuoteVariantPriceService(variantPriceSetLinkQueryPort, calculatePricesUseCase, idGenerator);
    }

    @Bean
    public QuoteVariantPricesUseCase quoteVariantPricesUseCase(
            VariantPriceSetLinkQueryPort variantPriceSetLinkQueryPort,
            PriceQueryPort priceQueryPort,
            CalculatePricesPolicy calculatePricesPolicy,
            IdGenerator idGenerator,
            QuoteVariantPricesPolicy quoteVariantPricesPolicy
    ) {
        return new QuoteVariantPricesService(
                variantPriceSetLinkQueryPort,
                priceQueryPort,
                calculatePricesPolicy,
                idGenerator,
                quoteVariantPricesPolicy
        );
    }

    @Bean
    public ListVariantIdsForPriceSetUseCase listVariantIdsForPriceSetUseCase(
            VariantPriceSetLinkQueryPort variantPriceSetLinkQueryPort
    ) {
        return new ListVariantIdsForPriceSetService(variantPriceSetLinkQueryPort);
    }
}


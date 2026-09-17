package com.grab.store.merchant.internal.api.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grab.store.merchant.internal.api.rest.assembler.StorefrontModelAssembler;
import com.grab.store.merchant.internal.api.rest.dto.request.CreateStorefrontRequest;
import com.grab.store.merchant.internal.api.rest.dto.request.UpdateStorefrontProfileRequest;
import com.grab.store.merchant.internal.api.rest.dto.response.StorefrontResponse;
import com.grab.store.merchant.internal.api.rest.service.AuthenticatedMerchantScopeResolver;
import com.grab.store.merchant.internal.api.rest.service.StorefrontCommandService;
import com.grab.store.merchant.internal.api.rest.service.StorefrontQueryService;
import com.grab.store.merchant.internal.exception.MerchantServiceError;
import com.grab.store.merchant.internal.exception.MerchantServiceException;
import com.grab.store.shared.security.WebMvcSecurityTestConfiguration;
import com.merchant.domain.exception.MerchantDomainError;
import com.merchant.domain.exception.MerchantDomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StorefrontController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(WebMvcSecurityTestConfiguration.class)
@TestPropertySource(properties = "merchant.enabled=true")
class StorefrontControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StorefrontCommandService commands;

    @MockBean
    private StorefrontQueryService queries;

    @MockBean
    private StorefrontModelAssembler assembler;

    @MockBean
    private AuthenticatedMerchantScopeResolver merchantScopes;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private StorefrontResponse response;

    @BeforeEach
    void setUp() {
        response = new StorefrontResponse(
                "sf-1",
                "merchant-1",
                "Main Shop",
                "main-shop",
                "DRAFT",
                null,
                Instant.parse("2026-09-17T00:00:00Z"),
                Instant.parse("2026-09-17T00:00:00Z"),
                0
        );
        when(merchantScopes.resolveCurrentMerchantId(any())).thenReturn("merchant-1");
        when(assembler.toModel(any(StorefrontResponse.class)))
                .thenAnswer(invocation -> EntityModel.of(invocation.getArgument(0)));
        when(assembler.toCollectionModel(any()))
                .thenAnswer(invocation -> CollectionModel.of(
                        ((List<StorefrontResponse>) invocation.getArgument(0)).stream()
                                .map(EntityModel::of)
                                .toList()
                ));
    }

    @Test
    void create_shouldReturn201WithSelfLink() throws Exception {
        when(commands.create(eq("merchant-1"), any(CreateStorefrontRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/merchants/storefronts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateStorefrontRequest("Main Shop", "main-shop"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.storefrontId").value("sf-1"))
                .andExpect(jsonPath("$.slug").value("main-shop"));
    }

    @Test
    void create_withDuplicateSlug_shouldReturn409() throws Exception {
        when(commands.create(eq("merchant-1"), any(CreateStorefrontRequest.class)))
                .thenThrow(new MerchantDomainException(
                        new MerchantDomainError.DuplicateSlug("main-shop"),
                        "Storefront slug is already used"
                ));

        mockMvc.perform(post("/api/v1/merchants/storefronts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateStorefrontRequest("Main Shop", "main-shop"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void list_withoutMerchantScope_shouldReturn403() throws Exception {
        when(merchantScopes.resolveCurrentMerchantId(any())).thenThrow(new MerchantServiceException(
                new MerchantServiceError.MerchantScopeForbidden("CUSTOMER_APP", "merchant.storefront", "sf-1"),
                "A Seller Portal merchant account scope is required"
        ));

        mockMvc.perform(get("/api/v1/merchants/storefronts"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void update_shouldReturn200() throws Exception {
        StorefrontResponse updated = new StorefrontResponse(
                "sf-1", "merchant-1", "Renamed", "renamed-shop", "DRAFT", null,
                response.createdAt(), response.updatedAt(), 1
        );
        when(commands.update(eq("sf-1"), eq("merchant-1"), any(UpdateStorefrontProfileRequest.class)))
                .thenReturn(updated);

        mockMvc.perform(patch("/api/v1/merchants/storefronts/sf-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new UpdateStorefrontProfileRequest("Renamed", "renamed-shop"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Renamed"));
    }

    @Test
    void activate_shouldReturn200() throws Exception {
        StorefrontResponse activated = new StorefrontResponse(
                "sf-1", "merchant-1", "Main Shop", "main-shop", "ACTIVE", null,
                response.createdAt(), response.updatedAt(), 1
        );
        when(commands.changeLifecycle(eq("sf-1"), eq("merchant-1"), any(), any())).thenReturn(activated);

        mockMvc.perform(post("/api/v1/merchants/storefronts/sf-1/activate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }
}

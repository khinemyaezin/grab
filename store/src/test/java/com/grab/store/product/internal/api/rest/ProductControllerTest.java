package com.grab.store.product.internal.api.rest;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grab.store.product.internal.api.rest.dto.CreateProductRequest;
import com.grab.store.product.internal.api.rest.dto.VariationDto;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
@EnabledIfSystemProperty(named = "test.integration", matches = "true")
class ProductControllerTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void postProduct() throws Exception {
        var yellow = VariationDto.builder()
                .optionId("94f86eab-8be2-4cee-9303-3340728fcda7")
                .optionName("Yellow")
                .typeId("b7922f9f-1fe9-4e51-9949-982a7398c92a")
                .typeName("Color").build();

        var small = VariationDto.builder()
                .optionId("128cb594-740c-4735-89f3-c930d2af0b7e")
                .optionName("Small")
                .typeId("71269f58-4d53-4f08-8678-74487ad99217")
                .typeName("Size").build();

        var variant = CreateProductRequest.Variant.builder()
                .sku("sku-1")
                .variations(List.of(yellow,small))
                .build();

        var product = CreateProductRequest.builder()
                .name("Shirt")
                .categoryId("bc67-5678-1234-5678")
                .variants(List.of(variant))
                .build();

        mvc.perform(MockMvcRequestBuilders.post("/api/v1/product")
                        .content(objectMapper.writeValueAsString(product))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
    }
}
package com.grab.store.product.internal.usecase.handler;

import com.grab.store.product.internal.command.UpdateProductCommand;
import com.grab.store.product.internal.command.VariationCommand;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

@EnabledIfSystemProperty(named = "test.integration", matches = "true")
@SpringBootTest
@Sql({"/mock-product.sql"})
class UpdateProductUseCaseHandlerTest {
    @Autowired
    UpdateProductUseCaseHandler updateProductUseCaseHandler;

    @Test
    void handle() {
        // color
        VariationCommand yellow = new VariationCommand("94f86eab-8be2-4cee-9303-3340728fcda7", "Yellow",
                "b7922f9f-1fe9-4e51-9949-982a7398c92a", "Color");
        VariationCommand blue = new VariationCommand("8578eda9-fc45-44fe-af5d-0ded9a9bc54d", "Blue",
                "b7922f9f-1fe9-4e51-9949-982a7398c92a", "Color");

        // size
        VariationCommand small = new VariationCommand("128cb594-740c-4735-89f3-c930d2af0b7e", "Small",
                "71269f58-4d53-4f08-8678-74487ad99217", "Size");

        UpdateProductCommand.VariantCommand smallYellow = new UpdateProductCommand.VariantCommand("2c095488-b386-4861-8234-cb8896a401a1", "SKU-1", List.of(small, yellow));
        UpdateProductCommand.VariantCommand smallBlue = new UpdateProductCommand.VariantCommand("SKU-2", List.of(small, blue));

        UpdateProductCommand updateProductCommand = new UpdateProductCommand("c9ac1163-e67e-4ea4-ad40-2eaf093d6b76",
                "Polo Shirt", "bc67-5678-1234-5678", List.of(smallYellow,smallBlue));

        this.updateProductUseCaseHandler.handle(updateProductCommand);
    }
}
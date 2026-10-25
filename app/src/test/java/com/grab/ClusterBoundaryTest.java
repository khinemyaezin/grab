package com.grab;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class ClusterBoundaryTest {
    private static final List<List<String>> CLUSTERS = List.of(
            List.of("catalog", "inventory", "pricing"),
            List.of("cart", "workflows"),
            List.of("identity", "merchant", "customer"),
            List.of("saleschannel", "region", "storefrontquery")
    );

    @Test
    void contextsDoNotImportAnotherContextsInternalPackages() {
        JavaClasses classes = new ClassFileImporter()
                .withImportOption(new ImportOption.DoNotIncludeTests())
                .importPackages("com.grab.app");

        for (int clusterIndex = 0; clusterIndex < CLUSTERS.size(); clusterIndex++) {
            String cluster = switch (clusterIndex) {
                case 0 -> "productmerchandising";
                case 1 -> "ordermanagement";
                case 2 -> "identityaccount";
                default -> "channeldiscovery";
            };
            for (String source : CLUSTERS.get(clusterIndex)) {
                for (List<String> targetCluster : CLUSTERS) {
                    for (String target : targetCluster) {
                        if (source.equals(target)) continue;
                        ArchRule rule = noClasses()
                                .that().resideInAPackage("com.grab.app." + cluster + "." + source + "..")
                                .should().dependOnClassesThat()
                                .resideInAPackage(".." + target + ".internal..");
                        rule.check(classes);
                    }
                }
            }
        }
    }
}

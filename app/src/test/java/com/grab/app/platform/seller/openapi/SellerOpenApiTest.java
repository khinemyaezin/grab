package com.grab.app.platform.seller.openapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.grab.app.Application;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.config.OpenApiConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.modulith.runtime.ApplicationRuntime;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Constructor;
import java.util.HashSet;
import java.util.Map;
import java.util.Locale;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

class SellerOpenApiTest {

    private static final String CONTROLLER_PACKAGE =
            "com.grab.app.platform.seller.internal.adapter.inbound.rest.controller";

    @Test
    void documentsEverySellerControllerAndMappedOperation() throws Exception {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(RestController.class));
        Set<BeanDefinition> candidates = scanner.findCandidateComponents(CONTROLLER_PACKAGE);
        Set<Class<?>> controllerTypes = new HashSet<>();
        Set<Class<?>> dependencies = new HashSet<>();
        WebApplicationContextRunner runner = new WebApplicationContextRunner()
                .withUserConfiguration(WebConfiguration.class)
                .withPropertyValues("spring.main.banner-mode=off");
        for (BeanDefinition candidate : candidates) {
            String className = candidate.getBeanClassName();
            Class<?> controllerType = Class.forName(className);
            controllerTypes.add(controllerType);
            runner = runner.withBean(controllerType);
            for (Constructor<?> constructor : controllerType.getConstructors()) {
                for (Class<?> dependency : constructor.getParameterTypes()) {
                    dependencies.add(dependency);
                }
            }
        }
        for (Class<?> dependency : dependencies) {
            runner = withMock(runner, dependency);
        }
        runner.run(context -> {
            assertThat(context).hasNotFailed();
            RequestMappingHandlerMapping mappings = context.getBean("requestMappingHandlerMapping", RequestMappingHandlerMapping.class);
            Map<RequestMappingInfo, HandlerMethod> methods = mappings.getHandlerMethods();
            Set<Class<?>> mappedControllers = new HashSet<>();
            MockMvc mvc = MockMvcBuilders.webAppContextSetup(context).build();
            MvcResult result = mvc.perform(get("/v3/api-docs")).andReturn();
            assertThat(result.getResponse().getStatus()).isEqualTo(200);
            String body = result.getResponse().getContentAsString();
            ObjectMapper mapper = new ObjectMapper();
            JsonNode document = mapper.readTree(body);
            JsonNode paths = document.path("paths");
            for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : methods.entrySet()) {
                Class<?> beanType = entry.getValue().getBeanType();
                if (!controllerTypes.contains(beanType)) {
                    continue;
                }
                mappedControllers.add(beanType);
                RequestMappingInfo mapping = entry.getKey();
                for (String path : mapping.getPatternValues()) {
                    for (RequestMethod method : mapping.getMethodsCondition().getMethods()) {
                        String verb = method.name().toLowerCase(Locale.ROOT);
                        JsonNode operation = paths.path(path).path(verb);
                        assertThat(operation.isMissingNode()).as("%s %s", verb, path).isFalse();
                    }
                }
            }
            assertThat(controllerTypes).isNotEmpty();
            assertThat(mappedControllers).containsExactlyInAnyOrderElementsOf(controllerTypes);
        });
    }

    private static <T> WebApplicationContextRunner withMock(WebApplicationContextRunner runner, Class<T> type) {
        return runner.withBean(type, () -> mock(type));
    }

    @Configuration(proxyBeanMethods = false)
    @EnableAutoConfiguration(exclude = DataSourceAutoConfiguration.class)
    @Import(OpenApiConfiguration.class)
    static class WebConfiguration {
        @Bean
        static ApplicationRuntime applicationRuntime() {
            return new ApplicationRuntime() {
                @Override
                public String getId() {
                    return "seller-openapi-test";
                }

                @Override
                public Class<?> getMainApplicationClass() {
                    return Application.class;
                }

                @Override
                public Class<?> getUserClass(Object bean, String beanName) {
                    return AopUtils.getTargetClass(bean);
                }

                @Override
                public boolean isApplicationClass(Class<?> type) {
                    String packageName = type.getPackageName();
                    return packageName.startsWith("com.grab.app.");
                }
            };
        }
    }
}

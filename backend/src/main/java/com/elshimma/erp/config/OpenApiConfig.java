package com.elshimma.erp.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_AUTH = "BearerAuth";

    @Bean
    public OpenApiCustomizer bearerSecuritySchemeCustomizer() {
        return openApi -> {
            if (openApi.getComponents() == null) {
                openApi.setComponents(new Components());
            }

            openApi.getComponents().addSecuritySchemes(
                    BEARER_AUTH,
                    new SecurityScheme()
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT")
                            .description("Enter the JWT token only. Swagger UI adds the Bearer prefix.")
            );
        };
    }

    @Bean
    public OpenApiCustomizer bearerSecurityRequirementCustomizer() {
        return openApi -> openApi.addSecurityItem(
                new io.swagger.v3.oas.models.security.SecurityRequirement().addList(BEARER_AUTH)
        );
    }

    @Bean
    public GroupedOpenApi authApi(OpenApiCustomizer bearerSecuritySchemeCustomizer) {
        return GroupedOpenApi.builder()
                .group("auth")
                .pathsToMatch("/api/auth/**")
                .addOpenApiCustomizer(bearerSecuritySchemeCustomizer)
                .build();
    }

    @Bean
    public GroupedOpenApi productApi(
            OpenApiCustomizer bearerSecuritySchemeCustomizer,
            OpenApiCustomizer bearerSecurityRequirementCustomizer
    ) {
        return GroupedOpenApi.builder()
                .group("product")
                .pathsToMatch("/api/products/**", "/api/categories/**")
                .addOpenApiCustomizer(bearerSecuritySchemeCustomizer)
                .addOpenApiCustomizer(bearerSecurityRequirementCustomizer)
                .build();
    }

    @Bean
    public GroupedOpenApi customerApi(
            OpenApiCustomizer bearerSecuritySchemeCustomizer,
            OpenApiCustomizer bearerSecurityRequirementCustomizer
    ) {
        return GroupedOpenApi.builder()
                .group("customers")
                .pathsToMatch("/api/customers/**")
                .addOpenApiCustomizer(bearerSecuritySchemeCustomizer)
                .addOpenApiCustomizer(bearerSecurityRequirementCustomizer)
                .build();
    }

    @Bean
    public GroupedOpenApi inventoryApi(
            OpenApiCustomizer bearerSecuritySchemeCustomizer,
            OpenApiCustomizer bearerSecurityRequirementCustomizer
    ) {
        return GroupedOpenApi.builder()
                .group("inventory")
                .pathsToMatch("/api/inventory/**", "/api/warehouses/**")
                .addOpenApiCustomizer(bearerSecuritySchemeCustomizer)
                .addOpenApiCustomizer(bearerSecurityRequirementCustomizer)
                .build();
    }

    @Bean
    public GroupedOpenApi supplierApi(
            OpenApiCustomizer bearerSecuritySchemeCustomizer,
            OpenApiCustomizer bearerSecurityRequirementCustomizer
    ) {
        return GroupedOpenApi.builder()
                .group("suppliers")
                .pathsToMatch("/api/suppliers/**")
                .addOpenApiCustomizer(bearerSecuritySchemeCustomizer)
                .addOpenApiCustomizer(bearerSecurityRequirementCustomizer)
                .build();
    }

    @Bean
    public GroupedOpenApi advancedWarehouseApi(
            OpenApiCustomizer bearerSecuritySchemeCustomizer,
            OpenApiCustomizer bearerSecurityRequirementCustomizer
    ) {
        return GroupedOpenApi.builder()
                .group("advanced-warehouse")
                .pathsToMatch("/api/warehouse/**")
                .addOpenApiCustomizer(bearerSecuritySchemeCustomizer)
                .addOpenApiCustomizer(bearerSecurityRequirementCustomizer)
                .build();
    }

    @Bean
    public GroupedOpenApi hrApi(
            OpenApiCustomizer bearerSecuritySchemeCustomizer,
            OpenApiCustomizer bearerSecurityRequirementCustomizer
    ) {
        return GroupedOpenApi.builder()
                .group("hr")
                .pathsToMatch("/api/hr/**")
                .addOpenApiCustomizer(bearerSecuritySchemeCustomizer)
                .addOpenApiCustomizer(bearerSecurityRequirementCustomizer)
                .build();
    }

    @Bean
    public GroupedOpenApi ordersApi(
            OpenApiCustomizer bearerSecuritySchemeCustomizer,
            OpenApiCustomizer bearerSecurityRequirementCustomizer
    ) {
        return GroupedOpenApi.builder()
                .group("orders")
                .pathsToMatch("/api/orders/**")
                .addOpenApiCustomizer(bearerSecuritySchemeCustomizer)
                .addOpenApiCustomizer(bearerSecurityRequirementCustomizer)
                .build();
    }

    @Bean
    public GroupedOpenApi productionApi(
            OpenApiCustomizer bearerSecuritySchemeCustomizer,
            OpenApiCustomizer bearerSecurityRequirementCustomizer
    ) {
        return GroupedOpenApi.builder()
                .group("production")
                .pathsToMatch("/api/production/**")
                .addOpenApiCustomizer(bearerSecuritySchemeCustomizer)
                .addOpenApiCustomizer(bearerSecurityRequirementCustomizer)
                .build();
    }

    @Bean
    public GroupedOpenApi financeApi(
            OpenApiCustomizer bearerSecuritySchemeCustomizer,
            OpenApiCustomizer bearerSecurityRequirementCustomizer
    ) {
        return GroupedOpenApi.builder()
                .group("finance")
                .pathsToMatch("/api/finance/**")
                .addOpenApiCustomizer(bearerSecuritySchemeCustomizer)
                .addOpenApiCustomizer(bearerSecurityRequirementCustomizer)
                .build();
    }

    @Bean
    public GroupedOpenApi analyticsApi(
            OpenApiCustomizer bearerSecuritySchemeCustomizer,
            OpenApiCustomizer bearerSecurityRequirementCustomizer
    ) {
        return GroupedOpenApi.builder()
                .group("analytics")
                .pathsToMatch("/api/analytics/**")
                .addOpenApiCustomizer(bearerSecuritySchemeCustomizer)
                .addOpenApiCustomizer(bearerSecurityRequirementCustomizer)
                .build();
    }
}

package com.gskart.commons.web.autoconfigure;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * Declares the bearer-token scheme once, so every service's generated API documentation offers the
 * same "Authorize" box and marks its operations as needing a token.
 *
 * <p>Applied through a customizer rather than annotations: an annotation on a library class isn't
 * reliably picked up, and the title and version belong to the individual service anyway.
 */
@AutoConfiguration
@ConditionalOnClass(OpenApiCustomizer.class)
@ConditionalOnProperty(prefix = "gskart.commons.web", name = "openapi.enabled", havingValue = "true",
        matchIfMissing = true)
public class CommonsOpenApiAutoConfiguration {

    static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    @ConditionalOnMissingBean(name = "gskartBearerAuthOpenApiCustomizer")
    public OpenApiCustomizer gskartBearerAuthOpenApiCustomizer() {
        return openApi -> {
            Components components = openApi.getComponents() == null ? new Components() : openApi.getComponents();
            components.addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT"));
            openApi.setComponents(components);
            openApi.addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
        };
    }
}

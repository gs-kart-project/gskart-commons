package com.gskart.commons.web.autoconfigure;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.junit.jupiter.api.Test;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class CommonsOpenApiAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(CommonsOpenApiAutoConfiguration.class));

    @Test
    void documentsTheBearerSchemeAndRequiresItGlobally() {
        runner.run(context -> {
            OpenAPI openApi = new OpenAPI();

            context.getBean(OpenApiCustomizer.class).customise(openApi);

            SecurityScheme scheme = openApi.getComponents().getSecuritySchemes().get("bearerAuth");
            assertThat(scheme.getType()).isEqualTo(SecurityScheme.Type.HTTP);
            assertThat(scheme.getScheme()).isEqualTo("bearer");
            assertThat(scheme.getBearerFormat()).isEqualTo("JWT");
            assertThat(openApi.getSecurity()).hasSize(1);
            assertThat(openApi.getSecurity().getFirst()).containsKey("bearerAuth");
        });
    }

    @Test
    void keepsWhateverTheServiceAlreadyDocumented() {
        runner.run(context -> {
            OpenAPI openApi = new OpenAPI().components(
                    new io.swagger.v3.oas.models.Components().addSchemas("Product",
                            new io.swagger.v3.oas.models.media.Schema<>().description("A catalog product")));

            context.getBean(OpenApiCustomizer.class).customise(openApi);

            assertThat(openApi.getComponents().getSchemas()).containsKey("Product");
            assertThat(openApi.getComponents().getSecuritySchemes()).containsKey("bearerAuth");
        });
    }

    @Test
    void contributesNothingWithoutSpringdoc() {
        runner.withClassLoader(new FilteredClassLoader(OpenApiCustomizer.class))
                .run(context -> assertThat(context).doesNotHaveBean("gskartBearerAuthOpenApiCustomizer"));
    }

    @Test
    void contributesNothingWhenSwitchedOff() {
        runner.withPropertyValues("gskart.commons.web.openapi.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(OpenApiCustomizer.class));
    }
}

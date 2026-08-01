package com.gskart.commons.security.autoconfigure;

import com.gskart.commons.security.GSKartResourceServerUserContext;
import com.gskart.commons.security.ResourceServerHttpSecurityCustomizer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import static org.assertj.core.api.Assertions.assertThat;

class CommonsSecurityAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(CommonsSecurityAutoConfiguration.class));

    @Test
    void contributesTheUserContextConverterAndCustomizer() {
        runner.run(context -> assertThat(context)
                .hasSingleBean(GSKartResourceServerUserContext.class)
                .hasSingleBean(ResourceServerHttpSecurityCustomizer.class)
                .hasSingleBean(JwtAuthenticationConverter.class));
    }

    @Test
    void mapsTheRolesClaimOntoAuthoritiesWithoutAPrefix() {
        runner.run(context -> {
            JwtAuthenticationConverter converter = context.getBean(JwtAuthenticationConverter.class);
            Jwt jwt = Jwt.withTokenValue("token")
                    .header("alg", "RS256")
                    .claim("sub", "gautham")
                    .claim("roles", java.util.List.of("Developer", "Customer"))
                    .build();

            // Spring Security adds an authority of its own describing how the caller authenticated,
            // so this checks the role mapping rather than the whole set.
            assertThat(converter.convert(jwt).getAuthorities())
                    .extracting("authority")
                    .contains("Developer", "Customer");
        });
    }

    @Test
    void honoursAServiceThatConfiguresItsOwnClaimMapping() {
        runner.withPropertyValues(
                        "gskart.commons.security.jwt.authorities-claim-name=scope",
                        "gskart.commons.security.jwt.authority-prefix=SCOPE_")
                .run(context -> {
                    JwtAuthenticationConverter converter = context.getBean(JwtAuthenticationConverter.class);
                    Jwt jwt = Jwt.withTokenValue("token")
                            .header("alg", "RS256")
                            .claim("sub", "gautham")
                            .claim("scope", "catalog.read")
                            .build();

                    assertThat(converter.convert(jwt).getAuthorities())
                            .extracting("authority")
                            .contains("SCOPE_catalog.read");
                });
    }

    @Test
    void backsOffWhenTheServiceDeclaresItsOwnBeans() {
        runner.withUserConfiguration(ServiceOwnBeans.class).run(context -> {
            assertThat(context).hasSingleBean(GSKartResourceServerUserContext.class);
            assertThat(context.getBean(GSKartResourceServerUserContext.class))
                    .isSameAs(context.getBean("serviceUserContext"));
            assertThat(context.getBean(JwtAuthenticationConverter.class))
                    .isSameAs(context.getBean("serviceConverter"));
        });
    }

    @Test
    void contributesNothingWhenSwitchedOff() {
        runner.withPropertyValues("gskart.commons.security.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(GSKartResourceServerUserContext.class));
    }

    @Test
    void contributesNothingWithoutSpringSecurityOnTheClasspath() {
        runner.withClassLoader(new FilteredClassLoader(SecurityFilterChain.class))
                .run(context -> assertThat(context).doesNotHaveBean(GSKartResourceServerUserContext.class));
    }

    @Configuration(proxyBeanMethods = false)
    static class ServiceOwnBeans {

        @Bean
        GSKartResourceServerUserContext serviceUserContext() {
            return new GSKartResourceServerUserContext();
        }

        @Bean
        JwtAuthenticationConverter serviceConverter() {
            JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
            converter.setJwtGrantedAuthoritiesConverter(jwt -> AuthorityUtils.NO_AUTHORITIES);
            return converter;
        }
    }
}

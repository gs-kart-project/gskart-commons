package com.gskart.commons.security.autoconfigure;

import com.gskart.commons.security.GSKartResourceServerUserContext;
import com.gskart.commons.security.ResourceServerHttpSecurityCustomizer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Wires the pieces a resource server needs: somewhere to keep the caller, the baseline
 * {@code HttpSecurity} configuration, and the claim-to-authority mapping.
 *
 * <p>It runs before Spring Boot's resource-server auto-configuration because both offer a
 * {@link JwtAuthenticationConverter} only when one is missing, and whoever is asked first wins.
 *
 * <p>No {@code SecurityFilterChain} is defined here on purpose. Spring Boot already contributes a
 * default chain when a service has none, so a second one would make the outcome depend on
 * auto-configuration order - and when it went wrong it would fail inside the service, not here.
 * Services build their own chain from
 * {@link ResourceServerHttpSecurityCustomizer}.
 */
@AutoConfiguration(before = OAuth2ResourceServerAutoConfiguration.class)
@ConditionalOnClass({SecurityFilterChain.class, JwtAuthenticationConverter.class})
@ConditionalOnProperty(prefix = "gskart.commons.security", name = "enabled", havingValue = "true",
        matchIfMissing = true)
@EnableConfigurationProperties(CommonsSecurityProperties.class)
public class CommonsSecurityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public GSKartResourceServerUserContext gskartResourceServerUserContext() {
        return new GSKartResourceServerUserContext();
    }

    @Bean
    @ConditionalOnMissingBean
    public ResourceServerHttpSecurityCustomizer resourceServerHttpSecurityCustomizer(
            GSKartResourceServerUserContext resourceServerUserContext) {
        return new ResourceServerHttpSecurityCustomizer(resourceServerUserContext);
    }

    /**
     * Maps the flat roles claim onto authorities without a prefix, so an authority check reads the
     * role name exactly as the token carries it.
     */
    @Bean
    @ConditionalOnMissingBean
    public JwtAuthenticationConverter jwtAuthenticationConverter(CommonsSecurityProperties properties) {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthoritiesClaimName(properties.jwt().authoritiesClaimName());
        grantedAuthoritiesConverter.setAuthorityPrefix(properties.jwt().authorityPrefix());

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }
}

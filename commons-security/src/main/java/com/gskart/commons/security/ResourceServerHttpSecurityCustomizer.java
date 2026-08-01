package com.gskart.commons.security;

import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;

/**
 * The resource-server baseline every GS Kart API shares: CORS on, CSRF off, no session, bearer
 * tokens, and the user-context filter sitting right after the token has been validated.
 *
 * <p>It deliberately says nothing about which routes need which authority. Who may call what is the
 * service's own decision and belongs where a reviewer reading that service will see it, not hidden
 * inside a dependency. The same goes for method security - each service keeps its own
 * {@code @EnableMethodSecurity}.
 *
 * <p>A service applies it and then adds its own rules:
 * <pre>
 * &#64;Bean
 * SecurityFilterChain securityFilterChain(HttpSecurity http, ResourceServerHttpSecurityCustomizer defaults) {
 *     defaults.customize(http);
 *     http.authorizeHttpRequests(requests -&gt; requests.anyRequest().authenticated());
 *     return http.build();
 * }
 * </pre>
 */
public class ResourceServerHttpSecurityCustomizer implements Customizer<HttpSecurity> {

    private final GSKartResourceServerUserContext resourceServerUserContext;

    public ResourceServerHttpSecurityCustomizer(GSKartResourceServerUserContext resourceServerUserContext) {
        this.resourceServerUserContext = resourceServerUserContext;
    }

    @Override
    public void customize(HttpSecurity http) {
        try {
            http.cors(Customizer.withDefaults())
                    .csrf(AbstractHttpConfigurer::disable)
                    .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    // The converter bean in the context is picked up automatically, so the role
                    // claim mapping applies without repeating it here.
                    .oauth2ResourceServer(resourceServer -> resourceServer.jwt(Customizer.withDefaults()))
                    .addFilterAfter(new JwtUserContextFilter(resourceServerUserContext),
                            BearerTokenAuthenticationFilter.class);
        } catch (Exception exception) {
            throw new IllegalStateException("Could not apply the GS Kart resource-server defaults.", exception);
        }
    }
}

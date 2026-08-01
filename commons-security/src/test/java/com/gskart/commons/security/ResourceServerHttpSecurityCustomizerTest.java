package com.gskart.commons.security;

import jakarta.servlet.Filter;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ResourceServerHttpSecurityCustomizerTest {

    private final ResourceServerHttpSecurityCustomizer customizer =
            new ResourceServerHttpSecurityCustomizer(new GSKartResourceServerUserContext());

    private static HttpSecurity builderStub() {
        return mock(HttpSecurity.class, Answers.RETURNS_SELF);
    }

    @Test
    void appliesTheResourceServerBaseline() {
        HttpSecurity http = builderStub();

        customizer.customize(http);

        verify(http).cors(any());
        verify(http).csrf(any());
        verify(http).sessionManagement(any());
        verify(http).oauth2ResourceServer(any());
    }

    @Test
    void addsTheUserContextFilterAfterTheBearerTokenFilter() {
        HttpSecurity http = builderStub();
        ArgumentCaptor<Filter> filter = ArgumentCaptor.forClass(Filter.class);

        customizer.customize(http);

        verify(http).addFilterAfter(filter.capture(), eq(BearerTokenAuthenticationFilter.class));
        assertThat(filter.getValue()).isInstanceOf(JwtUserContextFilter.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    void keepsTheApiStateless() {
        HttpSecurity http = builderStub();
        ArgumentCaptor<Customizer<SessionManagementConfigurer<HttpSecurity>>> sessionCustomizer =
                ArgumentCaptor.forClass(Customizer.class);

        customizer.customize(http);

        verify(http).sessionManagement(sessionCustomizer.capture());
        SessionManagementConfigurer<HttpSecurity> configurer = mock(SessionManagementConfigurer.class);
        sessionCustomizer.getValue().customize(configurer);
        verify(configurer).sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }

    @Test
    void reportsAConfigurationFailureAsAClearError() {
        HttpSecurity http = builderStub();
        when(http.cors(any())).thenThrow(new IllegalArgumentException("misconfigured"));

        assertThatThrownBy(() -> customizer.customize(http))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("resource-server defaults")
                .hasCauseInstanceOf(IllegalArgumentException.class);
    }
}

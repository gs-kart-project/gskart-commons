package com.gskart.commons.security;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtUserContextFilterTest {

    private final GSKartResourceServerUserContext userContext = new GSKartResourceServerUserContext();
    private final JwtUserContextFilter filter = new JwtUserContextFilter(userContext);

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
        userContext.clear();
    }

    private static Jwt token() {
        return Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .claim(ClaimNames.SUB, "gautham")
                .claim(ClaimNames.EMAIL, "gautham@gskart.test")
                .build();
    }

    @Test
    void publishesTheCallerForTheDurationOfTheRequest() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new JwtAuthenticationToken(token(), List.of(new SimpleGrantedAuthority("Developer"))));
        AtomicReference<GSKartResourceServerUser> seenDownstream = new AtomicReference<>();
        FilterChain chain = (request, response) -> seenDownstream.set(userContext.getGskartResourceServerUser());

        filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), chain);

        assertThat(seenDownstream.get()).isNotNull();
        assertThat(seenDownstream.get().getUsername()).isEqualTo("gautham");
        assertThat(seenDownstream.get().getEmail()).isEqualTo("gautham@gskart.test");
        assertThat(seenDownstream.get().getAuthorities()).extracting("authority").containsExactly("Developer");
    }

    @Test
    void leavesTheContextEmptyWhenTheRequestWasNotAuthenticatedWithAToken() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("someone", "secret", List.of()));
        AtomicReference<GSKartResourceServerUser> seenDownstream = new AtomicReference<>();
        FilterChain chain = (request, response) -> seenDownstream.set(userContext.getGskartResourceServerUser());

        filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), chain);

        assertThat(seenDownstream.get()).isNull();
    }

    @Test
    void clearsTheContextOnceTheRequestIsDone() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(token(), List.of()));

        filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), (request, response) -> {
        });

        assertThat(userContext.getGskartResourceServerUser()).isNull();
    }

    @Test
    void clearsTheContextEvenWhenTheRequestFails() {
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(token(), List.of()));
        FilterChain failing = (request, response) -> {
            throw new IllegalStateException("downstream blew up");
        };

        assertThatThrownBy(() -> filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), failing))
                .isInstanceOf(IllegalStateException.class);

        assertThat(userContext.getGskartResourceServerUser()).isNull();
    }
}

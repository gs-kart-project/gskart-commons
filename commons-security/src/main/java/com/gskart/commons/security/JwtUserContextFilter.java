package com.gskart.commons.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Copies the caller out of the security context into {@link GSKartResourceServerUserContext} once
 * the token has been validated, and clears it again on the way out.
 *
 * <p>It has to run inside the Spring Security chain, after the bearer-token filter has
 * authenticated the request - outside that chain the security context is still empty and the user
 * would come out null. {@link ResourceServerHttpSecurityCustomizer} places it correctly, which is
 * also why this filter is not published as a bean: a bare filter bean would be registered with the
 * servlet container as well, and the two registrations would race to be the one that runs.
 */
public class JwtUserContextFilter extends OncePerRequestFilter {

    private final GSKartResourceServerUserContext resourceServerUserContext;

    public JwtUserContextFilter(GSKartResourceServerUserContext resourceServerUserContext) {
        this.resourceServerUserContext = resourceServerUserContext;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {
                resourceServerUserContext.setGskartResourceServerUser(
                        new GSKartResourceServerUser(jwtAuthenticationToken.getToken(), jwtAuthenticationToken.getAuthorities()));
            }
            filterChain.doFilter(request, response);
        } finally {
            resourceServerUserContext.clear();
        }
    }
}

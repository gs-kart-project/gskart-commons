package com.gskart.commons.security.autoconfigure;

import com.gskart.commons.security.ClaimNames;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Settings for the shared resource-server wiring.
 *
 * @param enabled whether the library contributes its resource-server beans at all
 * @param jwt     how token claims map onto authorities
 */
@ConfigurationProperties(prefix = "gskart.commons.security")
public record CommonsSecurityProperties(@DefaultValue("true") boolean enabled, @DefaultValue Jwt jwt) {

    /**
     * @param authoritiesClaimName claim holding the caller's roles
     * @param authorityPrefix      prefix added to each authority; empty, because the roles are used
     *                             as-is in {@code hasAuthority(...)} checks
     */
    public record Jwt(@DefaultValue(ClaimNames.ROLES) String authoritiesClaimName,
                      @DefaultValue("") String authorityPrefix) {
    }
}

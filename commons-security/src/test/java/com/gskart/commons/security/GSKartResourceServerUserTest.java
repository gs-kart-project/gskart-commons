package com.gskart.commons.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GSKartResourceServerUserTest {

    private static Jwt token() {
        return Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .claim(ClaimNames.SUB, "gautham")
                .claim(ClaimNames.EMAIL, "gautham@gskart.test")
                .build();
    }

    @Test
    void readsTheUsernameAndEmailFromTheToken() {
        GSKartResourceServerUser user =
                new GSKartResourceServerUser(token(), List.of(new SimpleGrantedAuthority("Customer")));

        assertThat(user.getUsername()).isEqualTo("gautham");
        assertThat(user.getEmail()).isEqualTo("gautham@gskart.test");
        assertThat(user.getAuthorities()).extracting("authority").containsExactly("Customer");
    }

    @Test
    void treatsMissingAuthoritiesAsNone() {
        GSKartResourceServerUser user = new GSKartResourceServerUser(token(), null);

        assertThat(user.getAuthorities()).isEmpty();
    }

    @Test
    void reportsAnAccountThatIsAlwaysUsableAndCarriesNoPassword() {
        GSKartResourceServerUser user = new GSKartResourceServerUser(token(), List.of());

        assertThat(user.getPassword()).isEmpty();
        assertThat(user.isEnabled()).isTrue();
        assertThat(user.isAccountNonExpired()).isTrue();
        assertThat(user.isAccountNonLocked()).isTrue();
        assertThat(user.isCredentialsNonExpired()).isTrue();
    }
}

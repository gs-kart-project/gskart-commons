package com.gskart.commons.security;

/**
 * The JWT claims the auth service issues and every other service reads.
 *
 * <p>This is the one contract the library shares deliberately: the token format is a technical
 * agreement between the issuer and its resource servers, and having the names in three places
 * invites a silent mismatch the day one of them changes.
 */
public final class ClaimNames {

    public static final String SUB = "sub";

    public static final String EMAIL = "email";

    /** Flat list of role names, mapped straight onto authorities without a prefix. */
    public static final String ROLES = "roles";

    private ClaimNames() {
    }
}

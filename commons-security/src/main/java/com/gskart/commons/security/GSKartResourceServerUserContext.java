package com.gskart.commons.security;

/**
 * Holds the caller for the duration of one request, so code that is nowhere near the web layer -
 * setting createdBy on an entity, for example - can still find out who is asking.
 *
 * <p>Request threads come from a pool, so whatever puts a user in here has to take it out again in
 * a finally block; {@link JwtUserContextFilter} is what normally does that. Anything running off
 * the request thread (a Kafka listener, a scheduled job, an async callback) will find this empty by
 * design and must take the identity from the message it is processing instead.
 */
public class GSKartResourceServerUserContext {

    private final ThreadLocal<GSKartResourceServerUser> currentUser = new ThreadLocal<>();

    public void setGskartResourceServerUser(GSKartResourceServerUser gskartResourceServerUser) {
        currentUser.set(gskartResourceServerUser);
    }

    public GSKartResourceServerUser getGskartResourceServerUser() {
        return currentUser.get();
    }

    public void clear() {
        currentUser.remove();
    }
}

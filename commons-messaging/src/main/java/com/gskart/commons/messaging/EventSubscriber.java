package com.gskart.commons.messaging;

/**
 * The inbound half of the messaging port: something that consumes events of one type.
 *
 * <p>Provisional, and the only piece of this library with no implementation behind it yet. Today
 * every service listens with the broker's own annotations; this exists so the first service that
 * genuinely needs broker-independent consumption - notifications - has a shape to start from. Expect
 * it to change once that happens, and don't build on it before then.
 *
 * @param <T> the payload this subscriber understands
 */
public interface EventSubscriber<T> {

    /**
     * Handles one delivered event. Throwing tells the adapter the event was not processed, so it can
     * retry or route it aside according to its own policy.
     */
    void onEvent(T payload);
}

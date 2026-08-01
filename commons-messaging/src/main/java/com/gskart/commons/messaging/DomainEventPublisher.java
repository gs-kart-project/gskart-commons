package com.gskart.commons.messaging;

/**
 * Publishes an event without the caller knowing which broker carries it.
 *
 * <p>Publishing is synchronous: the call returns once the broker has accepted the event, or throws.
 * That is what lets an outbox relay decide whether to mark an entry as sent.
 */
public interface DomainEventPublisher {

    /**
     * @throws EventPublishException if the broker did not accept the event.
     */
    void publish(DomainEvent event);
}

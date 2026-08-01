package com.gskart.commons.messaging;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * An outbound event, described without reference to any particular broker. The adapter turns it into
 * whatever the transport expects, so a service changing broker changes one dependency rather than
 * every publishing call site.
 */
@Getter
@Builder
@ToString
public class DomainEvent {
    /** Where the event goes - a Kafka topic today. */
    private final String destination;
    /** Ordering key: events sharing a key keep their relative order. */
    private final String key;
    /** What happened, for logs and traces rather than for routing. */
    private final String eventType;
    /** The body; the adapter decides how to serialize it. */
    private final Object payload;
}

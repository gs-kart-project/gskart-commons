package com.gskart.commons.messaging;

/**
 * Thrown when the broker did not accept an event, so the caller can retry or leave the entry in its
 * outbox instead of assuming the event is on its way.
 */
public class EventPublishException extends RuntimeException {

    public EventPublishException(String message, Throwable cause) {
        super(message, cause);
    }
}

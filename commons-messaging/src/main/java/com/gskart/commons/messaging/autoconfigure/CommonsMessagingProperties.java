package com.gskart.commons.messaging.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

/**
 * Settings for the shared messaging adapters.
 *
 * @param kafka how the Kafka adapter behaves
 */
@ConfigurationProperties(prefix = "gskart.commons.messaging")
public record CommonsMessagingProperties(@DefaultValue Kafka kafka) {

    /**
     * @param sendTimeout how long a publish waits for the broker's acknowledgement before failing.
     *                    Five seconds keeps a request-scoped publish from hanging; a relay that can
     *                    afford to wait longer raises it.
     */
    public record Kafka(@DefaultValue("5s") Duration sendTimeout) {
    }
}

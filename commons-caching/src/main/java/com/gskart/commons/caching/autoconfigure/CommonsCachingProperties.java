package com.gskart.commons.caching.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

/**
 * Settings for the shared cache conventions.
 *
 * @param enabled     whether the library configures caching at all. Off unless asked for: caching
 *                    changes what a service returns, so it should never start because a dependency
 *                    was added.
 * @param timeToLive  how long an entry stays cached. Bounded on purpose - an entry with no expiry
 *                    outlives the data it copied.
 * @param keyPrefixed whether cache keys are prefixed with the application name, so services sharing
 *                    a Redis instance can't read or evict each other's entries.
 */
@ConfigurationProperties(prefix = "gskart.commons.caching")
public record CommonsCachingProperties(@DefaultValue("false") boolean enabled,
                                       @DefaultValue("10m") Duration timeToLive,
                                       @DefaultValue("true") boolean keyPrefixed) {
}

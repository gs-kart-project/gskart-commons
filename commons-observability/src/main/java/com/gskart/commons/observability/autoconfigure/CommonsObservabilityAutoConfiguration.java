package com.gskart.commons.observability.autoconfigure;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.micrometer.metrics.autoconfigure.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

/**
 * Tags every metric with the service that produced it and the environment it is running in, so one
 * dashboard can separate them instead of every service inventing its own labels.
 *
 * <p>Only instrumentation lives here. Which backend the metrics are shipped to, and whether tracing
 * is exported at all, stay with the service - a library that decides that decides the service's
 * runtime as well.
 */
@AutoConfiguration
@ConditionalOnClass(MeterRegistry.class)
@ConditionalOnProperty(prefix = "gskart.commons.observability", name = "common-tags.enabled",
        havingValue = "true", matchIfMissing = true)
public class CommonsObservabilityAutoConfiguration {

    static final String SERVICE_TAG = "service";
    static final String ENVIRONMENT_TAG = "env";

    private static final String UNKNOWN_SERVICE = "unknown";
    private static final String DEFAULT_ENVIRONMENT = "default";

    @Bean
    @ConditionalOnMissingBean(name = "gskartCommonTagsMeterRegistryCustomizer")
    public MeterRegistryCustomizer<MeterRegistry> gskartCommonTagsMeterRegistryCustomizer(Environment environment) {
        String service = environment.getProperty("spring.application.name", UNKNOWN_SERVICE);
        String[] activeProfiles = environment.getActiveProfiles();
        String activeEnvironment = activeProfiles.length > 0 ? activeProfiles[0] : DEFAULT_ENVIRONMENT;
        return registry -> registry.config().commonTags(SERVICE_TAG, service, ENVIRONMENT_TAG, activeEnvironment);
    }
}

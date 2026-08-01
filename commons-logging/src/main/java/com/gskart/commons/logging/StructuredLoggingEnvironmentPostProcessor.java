package com.gskart.commons.logging;

import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Switches console logging to structured JSON everywhere except a developer's machine, so logs
 * shipped to a central store arrive as fields rather than as text somebody has to parse.
 *
 * <p>This can't be an auto-configuration: logging is set up while the environment is being prepared,
 * long before any bean is created, and by then the format has already been chosen. An environment
 * post-processor runs early enough, which is the same hook Spring Boot uses to get configuration
 * files in place.
 *
 * <p>Only defaults are contributed, and they are added at the end of the property sources, so
 * anything the service sets - a file, an environment variable, a command-line argument - still wins.
 */
public class StructuredLoggingEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    static final String PROPERTY_SOURCE_NAME = "gskartCommonsStructuredLoggingDefaults";
    static final String CONSOLE_FORMAT_PROPERTY = "logging.structured.format.console";
    static final String SERVICE_NAME_PROPERTY = "logging.structured.ecs.service.name";
    static final String ENABLED_PROPERTY = "gskart.commons.logging.structured.enabled";

    private static final String LOCAL_PROFILE = "local";
    private static final String DEFAULT_FORMAT = "ecs";
    private static final String APPLICATION_NAME_PROPERTY = "spring.application.name";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (!environment.getProperty(ENABLED_PROPERTY, Boolean.class, Boolean.TRUE) || isLocal(environment)) {
            return;
        }
        Map<String, Object> defaults = new LinkedHashMap<>();
        if (!environment.containsProperty(CONSOLE_FORMAT_PROPERTY)) {
            defaults.put(CONSOLE_FORMAT_PROPERTY, DEFAULT_FORMAT);
        }
        if (!environment.containsProperty(SERVICE_NAME_PROPERTY)) {
            String applicationName = environment.getProperty(APPLICATION_NAME_PROPERTY);
            if (applicationName != null) {
                defaults.put(SERVICE_NAME_PROPERTY, applicationName);
            }
        }
        if (!defaults.isEmpty()) {
            environment.getPropertySources().addLast(new MapPropertySource(PROPERTY_SOURCE_NAME, defaults));
        }
    }

    private boolean isLocal(ConfigurableEnvironment environment) {
        for (String profile : environment.getActiveProfiles()) {
            if (LOCAL_PROFILE.equalsIgnoreCase(profile)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getOrder() {
        // Last, so the configuration files have been read and the active profiles are known by the
        // time this decides whether it is running locally.
        return Ordered.LOWEST_PRECEDENCE;
    }
}

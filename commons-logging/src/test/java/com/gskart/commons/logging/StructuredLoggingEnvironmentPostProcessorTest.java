package com.gskart.commons.logging;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class StructuredLoggingEnvironmentPostProcessorTest {

    private final StructuredLoggingEnvironmentPostProcessor postProcessor =
            new StructuredLoggingEnvironmentPostProcessor();
    private final SpringApplication application = new SpringApplication();

    private static ConfigurableEnvironment environmentWith(Map<String, Object> properties, String... profiles) {
        ConfigurableEnvironment environment = new StandardEnvironment();
        environment.setActiveProfiles(profiles);
        if (!properties.isEmpty()) {
            environment.getPropertySources().addFirst(new MapPropertySource("service", properties));
        }
        return environment;
    }

    @Test
    void logsAsJsonOnADeployedEnvironment() {
        ConfigurableEnvironment environment = environmentWith(Map.of(), "aws");

        postProcessor.postProcessEnvironment(environment, application);

        assertThat(environment.getProperty(StructuredLoggingEnvironmentPostProcessor.CONSOLE_FORMAT_PROPERTY))
                .isEqualTo("ecs");
    }

    @Test
    void keepsTheReadableConsoleWhileDevelopingLocally() {
        ConfigurableEnvironment environment = environmentWith(Map.of(), "local");

        postProcessor.postProcessEnvironment(environment, application);

        assertThat(environment.containsProperty(StructuredLoggingEnvironmentPostProcessor.CONSOLE_FORMAT_PROPERTY))
                .isFalse();
    }

    @Test
    void namesTheServiceAfterTheApplication() {
        ConfigurableEnvironment environment = environmentWith(Map.of("spring.application.name", "cart-service"), "aws");

        postProcessor.postProcessEnvironment(environment, application);

        assertThat(environment.getProperty(StructuredLoggingEnvironmentPostProcessor.SERVICE_NAME_PROPERTY))
                .isEqualTo("cart-service");
    }

    @Test
    void leavesTheServiceNameAloneWhenTheApplicationIsUnnamed() {
        ConfigurableEnvironment environment = environmentWith(Map.of(), "aws");

        postProcessor.postProcessEnvironment(environment, application);

        assertThat(environment.containsProperty(StructuredLoggingEnvironmentPostProcessor.SERVICE_NAME_PROPERTY))
                .isFalse();
    }

    @Test
    void neverOverridesWhatTheServiceAlreadyChose() {
        ConfigurableEnvironment environment = environmentWith(Map.of(
                StructuredLoggingEnvironmentPostProcessor.CONSOLE_FORMAT_PROPERTY, "logstash",
                StructuredLoggingEnvironmentPostProcessor.SERVICE_NAME_PROPERTY, "chosen-by-hand"), "aws");

        postProcessor.postProcessEnvironment(environment, application);

        assertThat(environment.getProperty(StructuredLoggingEnvironmentPostProcessor.CONSOLE_FORMAT_PROPERTY))
                .isEqualTo("logstash");
        assertThat(environment.getProperty(StructuredLoggingEnvironmentPostProcessor.SERVICE_NAME_PROPERTY))
                .isEqualTo("chosen-by-hand");
        assertThat(environment.getPropertySources()
                .contains(StructuredLoggingEnvironmentPostProcessor.PROPERTY_SOURCE_NAME)).isFalse();
    }

    @Test
    void contributesNothingWhenSwitchedOff() {
        ConfigurableEnvironment environment = environmentWith(
                Map.of(StructuredLoggingEnvironmentPostProcessor.ENABLED_PROPERTY, "false"), "aws");

        postProcessor.postProcessEnvironment(environment, application);

        assertThat(environment.containsProperty(StructuredLoggingEnvironmentPostProcessor.CONSOLE_FORMAT_PROPERTY))
                .isFalse();
    }

    @Test
    void runsLateEnoughToSeeTheActiveProfiles() {
        assertThat(postProcessor.getOrder()).isEqualTo(Integer.MAX_VALUE);
    }
}

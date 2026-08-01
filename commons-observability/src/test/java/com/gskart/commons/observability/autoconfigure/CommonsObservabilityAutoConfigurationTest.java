package com.gskart.commons.observability.autoconfigure;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.micrometer.metrics.autoconfigure.MeterRegistryCustomizer;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class CommonsObservabilityAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(CommonsObservabilityAutoConfiguration.class));

    @SuppressWarnings("unchecked")
    private static MeterRegistry customized(org.springframework.context.ApplicationContext context) {
        MeterRegistry registry = new SimpleMeterRegistry();
        context.getBean(MeterRegistryCustomizer.class).customize(registry);
        registry.counter("orders.placed").increment();
        return registry;
    }

    @Test
    void tagsMetricsWithTheServiceAndEnvironment() {
        runner.withPropertyValues("spring.application.name=cart-service", "spring.profiles.active=aws")
                .run(context -> {
                    MeterRegistry registry = customized(context);

                    assertThat(registry.get("orders.placed").counter().getId().getTags())
                            .extracting("key", "value")
                            .contains(org.assertj.core.groups.Tuple.tuple("service", "cart-service"),
                                    org.assertj.core.groups.Tuple.tuple("env", "aws"));
                });
    }

    @Test
    void fallsBackToPlaceholdersWhenNeitherIsConfigured() {
        runner.run(context -> {
            MeterRegistry registry = customized(context);

            assertThat(registry.get("orders.placed").counter().getId().getTags())
                    .extracting("key", "value")
                    .contains(org.assertj.core.groups.Tuple.tuple("service", "unknown"),
                            org.assertj.core.groups.Tuple.tuple("env", "default"));
        });
    }

    @Test
    void standsAsideForAServiceThatTagsMetricsItself() {
        runner.withBean("gskartCommonTagsMeterRegistryCustomizer", MeterRegistryCustomizer.class,
                        () -> registry -> registry.config().commonTags("service", "chosen-by-hand"))
                .run(context -> {
                    MeterRegistry registry = customized(context);

                    assertThat(registry.get("orders.placed").counter().getId().getTag("service"))
                            .isEqualTo("chosen-by-hand");
                });
    }

    @Test
    void contributesNothingWithoutMicrometer() {
        runner.withClassLoader(new FilteredClassLoader(MeterRegistry.class))
                .run(context -> assertThat(context).doesNotHaveBean(MeterRegistryCustomizer.class));
    }

    @Test
    void contributesNothingWhenSwitchedOff() {
        runner.withPropertyValues("gskart.commons.observability.common-tags.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(MeterRegistryCustomizer.class));
    }
}

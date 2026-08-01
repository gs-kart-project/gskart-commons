package com.gskart.commons.messaging.autoconfigure;

import com.gskart.commons.messaging.DomainEvent;
import com.gskart.commons.messaging.DomainEventPublisher;
import com.gskart.commons.messaging.kafka.KafkaDomainEventPublisher;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class CommonsMessagingAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(CommonsMessagingAutoConfiguration.class))
            .withUserConfiguration(KafkaTemplateConfiguration.class);

    @Test
    void publishesThroughKafkaByDefault() {
        runner.run(context -> assertThat(context)
                .hasSingleBean(DomainEventPublisher.class)
                .getBean(DomainEventPublisher.class)
                .isInstanceOf(KafkaDomainEventPublisher.class));
    }

    @Test
    void standsAsideWhenTheServiceBringsItsOwnPublisher() {
        runner.withUserConfiguration(ServiceOwnPublisher.class).run(context -> {
            assertThat(context).hasSingleBean(DomainEventPublisher.class);
            assertThat(context.getBean(DomainEventPublisher.class)).isNotInstanceOf(KafkaDomainEventPublisher.class);
        });
    }

    @Test
    void staysOutOfTheWayWhenTheServiceIsOnAnotherBroker() {
        runner.withPropertyValues("gskart.messaging.broker=sqs")
                .run(context -> assertThat(context).doesNotHaveBean(DomainEventPublisher.class));
    }

    @Test
    void contributesNothingWithoutKafkaOnTheClasspath() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(CommonsMessagingAutoConfiguration.class))
                .withClassLoader(new FilteredClassLoader(KafkaTemplate.class))
                .run(context -> assertThat(context).doesNotHaveBean(DomainEventPublisher.class));
    }

    @Test
    void appliesTheConfiguredSendTimeout() {
        runner.withPropertyValues("gskart.commons.messaging.kafka.send-timeout=30s").run(context -> {
            CommonsMessagingProperties properties = context.getBean(CommonsMessagingProperties.class);
            assertThat(properties.kafka().sendTimeout()).hasSeconds(30);
        });
    }

    @Test
    void defaultsToAFiveSecondSendTimeout() {
        runner.run(context -> assertThat(
                context.getBean(CommonsMessagingProperties.class).kafka().sendTimeout()).hasSeconds(5));
    }

    @Configuration(proxyBeanMethods = false)
    static class KafkaTemplateConfiguration {

        @Bean
        @SuppressWarnings("unchecked")
        KafkaTemplate<String, Object> kafkaTemplate() {
            return mock(KafkaTemplate.class);
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class ServiceOwnPublisher {

        @Bean
        DomainEventPublisher serviceEventPublisher() {
            return (DomainEvent event) -> {
            };
        }
    }
}

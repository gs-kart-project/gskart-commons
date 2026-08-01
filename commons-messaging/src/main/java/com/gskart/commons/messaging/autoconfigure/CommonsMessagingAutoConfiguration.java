package com.gskart.commons.messaging.autoconfigure;

import com.gskart.commons.messaging.DomainEventPublisher;
import com.gskart.commons.messaging.kafka.KafkaDomainEventPublisher;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.kafka.autoconfigure.KafkaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

/**
 * Picks the adapter that backs {@link DomainEventPublisher}.
 *
 * <p>Kafka is the default and the only one so far. Which adapter is used is a property rather than a
 * code change, so moving a service onto a managed queue later means configuration plus a new
 * adapter - not edits to the services that publish.
 *
 * <p>It runs after Spring Boot has had its chance to create the {@code KafkaTemplate} this adapter
 * wraps.
 */
@AutoConfiguration(after = KafkaAutoConfiguration.class)
@EnableConfigurationProperties(CommonsMessagingProperties.class)
public class CommonsMessagingAutoConfiguration {

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(KafkaTemplate.class)
    @ConditionalOnProperty(name = "gskart.messaging.broker", havingValue = "kafka", matchIfMissing = true)
    static class KafkaConfiguration {

        @Bean
        @ConditionalOnBean(KafkaTemplate.class)
        @ConditionalOnMissingBean(DomainEventPublisher.class)
        DomainEventPublisher kafkaDomainEventPublisher(KafkaTemplate<String, Object> kafkaTemplate,
                CommonsMessagingProperties properties) {
            return new KafkaDomainEventPublisher(kafkaTemplate, properties.kafka().sendTimeout());
        }
    }
}

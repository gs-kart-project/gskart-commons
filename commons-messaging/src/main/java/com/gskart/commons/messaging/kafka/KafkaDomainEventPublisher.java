package com.gskart.commons.messaging.kafka;

import com.gskart.commons.messaging.DomainEvent;
import com.gskart.commons.messaging.DomainEventPublisher;
import com.gskart.commons.messaging.EventPublishException;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Publishes events through Kafka.
 *
 * <p>It waits for the broker's acknowledgement rather than firing and forgetting, because the caller
 * is usually an outbox relay deciding whether an entry can be marked as sent. The wait is bounded so
 * a broker that has stopped answering fails the call instead of holding the thread.
 *
 * <p>How the payload is serialized is the service's decision, made through its own Kafka
 * configuration.
 */
@Slf4j
public class KafkaDomainEventPublisher implements DomainEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final Duration sendTimeout;

    public KafkaDomainEventPublisher(KafkaTemplate<String, Object> kafkaTemplate, Duration sendTimeout) {
        this.kafkaTemplate = kafkaTemplate;
        this.sendTimeout = sendTimeout;
    }

    @Override
    public void publish(DomainEvent event) {
        ProducerRecord<String, Object> record =
                new ProducerRecord<>(event.getDestination(), event.getKey(), event.getPayload());
        try {
            kafkaTemplate.send(record).get(sendTimeout.toMillis(), TimeUnit.MILLISECONDS);
            log.info("Published {} event (key {}) to {}.",
                    event.getEventType(), event.getKey(), event.getDestination());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new EventPublishException("Interrupted while publishing to " + event.getDestination(), exception);
        } catch (ExecutionException | TimeoutException exception) {
            throw new EventPublishException(
                    "Failed to publish " + event.getEventType() + " event to " + event.getDestination(), exception);
        }
    }
}

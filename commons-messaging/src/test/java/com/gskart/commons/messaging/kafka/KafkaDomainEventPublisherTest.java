package com.gskart.commons.messaging.kafka;

import com.gskart.commons.messaging.DomainEvent;
import com.gskart.commons.messaging.EventPublishException;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class KafkaDomainEventPublisherTest {

    @SuppressWarnings("unchecked")
    private final KafkaTemplate<String, Object> kafkaTemplate = mock(KafkaTemplate.class);

    private final KafkaDomainEventPublisher publisher =
            new KafkaDomainEventPublisher(kafkaTemplate, Duration.ofSeconds(5));

    private static DomainEvent event() {
        return DomainEvent.builder()
                .destination("cart.update")
                .key("cart-42")
                .eventType("CART_UPDATE")
                .payload("{\"id\":42}")
                .build();
    }

    @Test
    void sendsTheEventToItsDestinationKeyedForOrdering() {
        when(kafkaTemplate.send(any(ProducerRecord.class)))
                .thenReturn(CompletableFuture.completedFuture(mock(SendResult.class)));
        ArgumentCaptor<ProducerRecord<String, Object>> record = ArgumentCaptor.forClass(ProducerRecord.class);

        publisher.publish(event());

        verify(kafkaTemplate).send(record.capture());
        assertThat(record.getValue().topic()).isEqualTo("cart.update");
        assertThat(record.getValue().key()).isEqualTo("cart-42");
        assertThat(record.getValue().value()).isEqualTo("{\"id\":42}");
    }

    @Test
    void reportsARejectedSendAsAPublishFailure() {
        when(kafkaTemplate.send(any(ProducerRecord.class))).thenReturn(
                CompletableFuture.failedFuture(new ExecutionException(new IllegalStateException("broker down"))));

        assertThatThrownBy(() -> publisher.publish(event()))
                .isInstanceOf(EventPublishException.class)
                .hasMessageContaining("cart.update");
    }

    @Test
    void givesUpOnceTheBrokerStopsAnswering() {
        KafkaDomainEventPublisher impatient =
                new KafkaDomainEventPublisher(kafkaTemplate, Duration.ofMillis(20));
        when(kafkaTemplate.send(any(ProducerRecord.class))).thenReturn(new CompletableFuture<>());

        assertThatThrownBy(() -> impatient.publish(event()))
                .isInstanceOf(EventPublishException.class)
                .hasMessageContaining("Failed to publish");
    }

    @Test
    void restoresTheInterruptFlagWhenTheWaitIsInterrupted() throws Exception {
        when(kafkaTemplate.send(any(ProducerRecord.class))).thenReturn(new CompletableFuture<>());
        Thread publishing = new Thread(() -> {
            try {
                new KafkaDomainEventPublisher(kafkaTemplate, Duration.ofSeconds(30)).publish(event());
            } catch (EventPublishException expected) {
                assertThat(Thread.currentThread().isInterrupted()).isTrue();
                assertThat(expected).hasMessageContaining("Interrupted");
            }
        });

        publishing.start();
        Thread.sleep(50);
        publishing.interrupt();
        publishing.join(2000);

        assertThat(publishing.isAlive()).isFalse();
    }
}

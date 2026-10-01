package com.archetype.kafka.infrastructure.adapter.out.kafka;

import com.archetype.kafka.domain.model.Transaction;
import com.archetype.kafka.domain.port.out.TransactionEventPublisherPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class TransactionKafkaPublisherAdapter implements TransactionEventPublisherPort {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String topic;

    public TransactionKafkaPublisherAdapter(KafkaTemplate<String, Object> kafkaTemplate,
                                            @Value("${app.kafka.topics.transaction-events}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    @Override
    public void publishTransactionCreatedEvent(Transaction transaction) {
        System.out.println("Publishing transaction event to Kafka: " + transaction.getId());
        kafkaTemplate.send(topic, transaction.getId(), transaction);
    }
}

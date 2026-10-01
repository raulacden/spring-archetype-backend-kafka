package com.archetype.kafka.domain.port.out;

import com.archetype.kafka.domain.model.Transaction;

public interface TransactionEventPublisherPort {
    void publishTransactionCreatedEvent(Transaction transaction);
}

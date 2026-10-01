package com.archetype.kafka.domain.port.out;

import com.archetype.kafka.domain.model.Transaction;

public interface FraudServicePort {
    boolean isFraudulent(Transaction transaction);
}

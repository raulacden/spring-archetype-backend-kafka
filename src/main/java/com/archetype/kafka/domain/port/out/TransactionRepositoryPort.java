package com.archetype.kafka.domain.port.out;

import com.archetype.kafka.domain.model.Transaction;

import java.util.Optional;

public interface TransactionRepositoryPort {
    Transaction save(Transaction transaction);
    Optional<Transaction> findById(String id);
}

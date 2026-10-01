package com.archetype.kafka.domain.port.in;

import com.archetype.kafka.domain.model.Transaction;

public interface CreateTransactionUseCase {
    Transaction createTransaction(Transaction transaction);
}

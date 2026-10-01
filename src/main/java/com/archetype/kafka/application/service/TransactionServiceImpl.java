package com.archetype.kafka.application.service;

import com.archetype.kafka.domain.model.Transaction;
import com.archetype.kafka.domain.model.TransactionStatus;
import com.archetype.kafka.domain.port.in.CreateTransactionUseCase;
import com.archetype.kafka.domain.port.out.FraudServicePort;
import com.archetype.kafka.domain.port.out.TransactionEventPublisherPort;
import com.archetype.kafka.domain.port.out.TransactionRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class TransactionServiceImpl implements CreateTransactionUseCase {

    private final TransactionRepositoryPort repositoryPort;
    private final FraudServicePort fraudServicePort;
    private final TransactionEventPublisherPort eventPublisherPort;

    public TransactionServiceImpl(TransactionRepositoryPort repositoryPort,
                                  FraudServicePort fraudServicePort,
                                  TransactionEventPublisherPort eventPublisherPort) {
        this.repositoryPort = repositoryPort;
        this.fraudServicePort = fraudServicePort;
        this.eventPublisherPort = eventPublisherPort;
    }

    @Override
    @Transactional
    public Transaction createTransaction(Transaction transaction) {
        transaction.setId(UUID.randomUUID().toString());
        transaction.setCreatedAt(LocalDateTime.now());
        
        // 1. Validate with External Service
        boolean isFraud = fraudServicePort.isFraudulent(transaction);
        if (isFraud) {
            transaction.setStatus(TransactionStatus.REJECTED);
        } else {
            transaction.setStatus(TransactionStatus.PENDING);
        }

        // 2. Save to Database via Port
        Transaction savedTransaction = repositoryPort.save(transaction);

        // 3. Publish Event to Kafka via Port
        eventPublisherPort.publishTransactionCreatedEvent(savedTransaction);

        return savedTransaction;
    }
}

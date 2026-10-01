package com.archetype.kafka.infrastructure.adapter.out.db;

import com.archetype.kafka.domain.model.Transaction;
import com.archetype.kafka.domain.port.out.TransactionRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class TransactionJpaAdapter implements TransactionRepositoryPort {

    private final TransactionJpaRepository repository;

    public TransactionJpaAdapter(TransactionJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Transaction save(Transaction transaction) {
        TransactionEntity entity = TransactionEntity.builder()
                .id(transaction.getId())
                .accountId(transaction.getAccountId())
                .amount(transaction.getAmount())
                .status(transaction.getStatus())
                .createdAt(transaction.getCreatedAt())
                .build();
        
        TransactionEntity savedEntity = repository.save(entity);
        
        return Transaction.builder()
                .id(savedEntity.getId())
                .accountId(savedEntity.getAccountId())
                .amount(savedEntity.getAmount())
                .status(savedEntity.getStatus())
                .createdAt(savedEntity.getCreatedAt())
                .build();
    }

    @Override
    public Optional<Transaction> findById(String id) {
        return repository.findById(id).map(entity -> Transaction.builder()
                .id(entity.getId())
                .accountId(entity.getAccountId())
                .amount(entity.getAmount())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .build());
    }
}

package com.archetype.kafka.infrastructure.adapter.out.db;

import com.archetype.kafka.domain.model.Transaction;
import com.archetype.kafka.domain.model.TransactionStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionJpaAdapterTest {

    @Mock
    private TransactionJpaRepository repository;

    @InjectMocks
    private TransactionJpaAdapter adapter;

    @Test
    void shouldSaveAndMapToDomain() {
        // Arrange
        Transaction domainTx = Transaction.builder()
                .id("123")
                .accountId("ACC-1")
                .amount(new BigDecimal("100"))
                .status(TransactionStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        TransactionEntity savedEntity = TransactionEntity.builder()
                .id(domainTx.getId())
                .accountId(domainTx.getAccountId())
                .amount(domainTx.getAmount())
                .status(domainTx.getStatus())
                .createdAt(domainTx.getCreatedAt())
                .build();

        when(repository.save(any(TransactionEntity.class))).thenReturn(savedEntity);

        // Act
        Transaction result = adapter.save(domainTx);

        // Assert
        assertEquals("123", result.getId());
        assertEquals(TransactionStatus.PENDING, result.getStatus());
        verify(repository).save(any(TransactionEntity.class));
    }

    @Test
    void shouldFindByIdAndMapToDomain() {
        // Arrange
        TransactionEntity entity = TransactionEntity.builder()
                .id("123")
                .accountId("ACC-1")
                .amount(new BigDecimal("100"))
                .status(TransactionStatus.APPROVED)
                .createdAt(LocalDateTime.now())
                .build();

        when(repository.findById("123")).thenReturn(Optional.of(entity));

        // Act
        Optional<Transaction> result = adapter.findById("123");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("123", result.get().getId());
        assertEquals(TransactionStatus.APPROVED, result.get().getStatus());
    }
}

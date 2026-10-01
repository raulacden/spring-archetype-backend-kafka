package com.archetype.kafka.application.service;

import com.archetype.kafka.domain.model.Transaction;
import com.archetype.kafka.domain.model.TransactionStatus;
import com.archetype.kafka.domain.port.out.FraudServicePort;
import com.archetype.kafka.domain.port.out.TransactionEventPublisherPort;
import com.archetype.kafka.domain.port.out.TransactionRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    private TransactionRepositoryPort repositoryPort;

    @Mock
    private FraudServicePort fraudServicePort;

    @Mock
    private TransactionEventPublisherPort eventPublisherPort;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private Transaction transaction;

    @BeforeEach
    void setUp() {
        transaction = Transaction.builder()
                .accountId("ACC-TEST")
                .amount(new BigDecimal("100.00"))
                .build();
    }

    @Test
    void shouldCreatePendingTransaction_whenNotFraudulent() {
        // Arrange
        when(fraudServicePort.isFraudulent(any(Transaction.class))).thenReturn(false);
        when(repositoryPort.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Transaction result = transactionService.createTransaction(transaction);

        // Assert
        assertNotNull(result.getId());
        assertNotNull(result.getCreatedAt());
        assertEquals(TransactionStatus.PENDING, result.getStatus());
        
        verify(fraudServicePort, times(1)).isFraudulent(any(Transaction.class));
        verify(repositoryPort, times(1)).save(any(Transaction.class));
        verify(eventPublisherPort, times(1)).publishTransactionCreatedEvent(any(Transaction.class));
    }

    @Test
    void shouldCreateRejectedTransaction_whenFraudulent() {
        // Arrange
        when(fraudServicePort.isFraudulent(any(Transaction.class))).thenReturn(true);
        when(repositoryPort.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Transaction result = transactionService.createTransaction(transaction);

        // Assert
        assertNotNull(result.getId());
        assertEquals(TransactionStatus.REJECTED, result.getStatus());
        
        verify(fraudServicePort, times(1)).isFraudulent(any(Transaction.class));
        verify(repositoryPort, times(1)).save(any(Transaction.class));
        verify(eventPublisherPort, times(1)).publishTransactionCreatedEvent(any(Transaction.class));
    }
}

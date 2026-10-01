package com.archetype.kafka.infrastructure.adapter.in.web;

import com.archetype.kafka.domain.model.Transaction;
import com.archetype.kafka.domain.model.TransactionStatus;
import com.archetype.kafka.domain.port.in.CreateTransactionUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CreateTransactionUseCase createTransactionUseCase;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldReturnCreatedTransaction_whenValidRequest() throws Exception {
        // Arrange
        TransactionRequest request = new TransactionRequest();
        request.setAccountId("ACC-123");
        request.setAmount(new BigDecimal("150.00"));

        Transaction mockTransaction = Transaction.builder()
                .id("tx-id-123")
                .accountId("ACC-123")
                .amount(new BigDecimal("150.00"))
                .status(TransactionStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        when(createTransactionUseCase.createTransaction(any(Transaction.class))).thenReturn(mockTransaction);

        // Act & Assert
        mockMvc.perform(post("/api/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("tx-id-123"))
                .andExpect(jsonPath("$.accountId").value("ACC-123"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void shouldReturnBadRequest_whenInvalidRequest() throws Exception {
        // Arrange: Amount is negative (invalid based on our validation)
        TransactionRequest request = new TransactionRequest();
        request.setAccountId("ACC-123");
        request.setAmount(new BigDecimal("-10.00"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}

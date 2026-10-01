package com.archetype.kafka.infrastructure.adapter.in.kafka;

import com.archetype.kafka.domain.model.Transaction;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class TransactionKafkaListenerAdapter {

    @KafkaListener(topics = "${app.kafka.topics.transaction-events}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeTransactionEvent(Transaction transaction, Acknowledgment acknowledgment) {
        System.out.println("==================================================");
        System.out.println("Consumed transaction event from Kafka! Transaction ID: " + transaction.getId());
        
        try {
            // Lógica simulada para provocar un error (Ej: accountId empieza por ERROR) - Esto SÍ reintenta
            if (transaction.getAccountId() != null && transaction.getAccountId().startsWith("ERROR")) {
                System.err.println("Simulating a processing error for account ERROR...");
                throw new RuntimeException("Simulated error to trigger retries and DLT!");
            }

            // Lógica simulada para provocar un error FATAL (No reintenta, va directo al DLT)
            if (transaction.getAccountId() != null && transaction.getAccountId().startsWith("FATAL")) {
                System.err.println("Simulating a fatal error that should NOT be retried...");
                throw new IllegalArgumentException("Data is completely invalid, do not retry!");
            }
            
            // Confirmamos que el mensaje fue procesado correctamente
            acknowledgment.acknowledge();
            System.out.println("Message processed and acknowledged successfully.");
            
        } catch (Exception e) {
            System.err.println("Error processing message: " + e.getMessage());
            // ¡IMPORTANTE! Para que el DefaultErrorHandler actúe (reintente y mande al DLT), 
            // NO debemos tragarnos la excepción. Debemos relanzarla.
            throw e; 
        }
    }

    /**
     * Listener dedicado a escuchar el Dead Letter Topic (DLT).
     * Por defecto, el DeadLetterPublishingRecoverer añade ".DLT" al nombre del topic original.
     */
    @KafkaListener(topics = "${app.kafka.topics.transaction-events}.DLT", groupId = "${spring.kafka.consumer.group-id}-dlt")
    public void consumeDlt(Transaction transaction, Acknowledgment acknowledgment) {
        System.err.println("================= DLT ALARM ======================");
        System.err.println("Received message in DLT! Transaction ID: " + transaction.getId());
        System.err.println("This message failed all retries and needs manual intervention.");
        
        // Aquí guardarías el evento en una tabla de auditoría, enviarías un email/alerta, etc.
        
        acknowledgment.acknowledge();
    }
}

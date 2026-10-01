package com.archetype.kafka.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConfig {

    /**
     * Configura el manejador de errores global para los consumidores de Kafka.
     * Define una política de reintentos y el envío a un Dead Letter Topic (DLT) si fallan todos los intentos.
     */
    @Bean
    public DefaultErrorHandler errorHandler(KafkaTemplate<Object, Object> kafkaTemplate) {
        // El DeadLetterPublishingRecoverer enviará el mensaje fallido a un topic con el mismo nombre + ".DLT"
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate);
        
        // Configuramos 3 reintentos locales con 1 segundo (1000ms) de espera entre cada intento
        FixedBackOff backOff = new FixedBackOff(1000L, 3L);
        
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, backOff);
        
        // Excepciones fatales que no se van a resolver por mucho que reintentemos.
        // Si ocurre alguna de estas, el mensaje irá directamente al DLT sin pasar por los 3 intentos.
        errorHandler.addNotRetryableExceptions(
            IllegalArgumentException.class,
            NullPointerException.class
        );
        
        return errorHandler;
    }
}

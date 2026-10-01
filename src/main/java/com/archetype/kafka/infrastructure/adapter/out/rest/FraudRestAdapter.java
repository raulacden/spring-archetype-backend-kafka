package com.archetype.kafka.infrastructure.adapter.out.rest;

import com.archetype.kafka.domain.model.Transaction;
import com.archetype.kafka.domain.port.out.FraudServicePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class FraudRestAdapter implements FraudServicePort {

    private final RestTemplate restTemplate;
    private final String fraudServiceUrl;

    public FraudRestAdapter(RestTemplate restTemplate, 
                            @Value("${app.external.fraud-service.url}") String fraudServiceUrl) {
        this.restTemplate = restTemplate;
        this.fraudServiceUrl = fraudServiceUrl;
    }

    @Override
    public boolean isFraudulent(Transaction transaction) {
        // Here we would typically make a real call:
        // FraudResponse response = restTemplate.postForObject(fraudServiceUrl, requestDto, FraudResponse.class);
        // return response.isFraud();
        
        // Mock implementation for the archetype example
        System.out.println("Calling external REST service to check fraud for account: " + transaction.getAccountId());
        
        // Simple logic: if amount is > 10000, consider it fraud
        return transaction.getAmount().doubleValue() > 10000.0;
    }
}

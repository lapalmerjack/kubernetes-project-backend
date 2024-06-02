package com.mooc.kubernetes;

import io.nats.client.Connection;
import io.nats.client.Nats;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class NatsService {


    @Value("${nats.url}")
    private String natsUrl;

    @Value("${nats.subject}")
    private String natsSubject;

    private static final Logger logger = LoggerFactory.getLogger(NatsService.class);

    private Connection natsConnection;




    @PostConstruct
    public void init() {
        try {
            logger.info("Connecting to NATS server at {}", natsUrl);
            Connection natsConnection = Nats.connect(natsUrl);
        } catch (IOException | InterruptedException e) {
            logger.error("Failed to connect to NATS server", e);
            throw new RuntimeException("Failed to connect to NATS server", e);
        }
        logger.info("Connected to Nats Server");
    }


    public void publishMessageToNats(String message) {
        try {

            natsConnection.publish(natsSubject, message.getBytes());
            logger.info("Published message to NATS: {}", message);
        } catch (Exception e) {
            logger.error("Failed to publish message to NATS", e);
        }
    }

}

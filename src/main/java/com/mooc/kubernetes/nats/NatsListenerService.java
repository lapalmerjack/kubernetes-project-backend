package com.mooc.kubernetes.nats;


import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.Nats;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;

@Service
public class NatsListenerService {

    private static final Logger logger = LoggerFactory.getLogger(NatsListenerService.class);


    @Value("${nats.url}")
    private String natsUrl;


    @Value("${nats.subject}")
    private String natsSubject;


    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.chat.id}")
    private String chatId;


    private final WebClient webClient;
    private Connection natsConnection;

    public NatsListenerService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://api.telegram.org").build();
    }

    @PostConstruct
    public void init() {
        try {
            this.natsConnection = Nats.connect(natsUrl);
            Dispatcher dispatcher = natsConnection.createDispatcher(msg -> {
                String message = new String(msg.getData());
                logger.info("Received message: {}", message);
                sendMessageToTelegram(message);
            });
            dispatcher.subscribe(natsSubject);
            logger.info("Subscribed to NATS subject: {}", natsSubject);
        } catch (IOException | InterruptedException e) {
            logger.error("Failed to connect to NATS server", e);
            throw new RuntimeException("Failed to connect to NATS server", e);
        }
    }

    public void publishMessageToNats(String message) {
        try {
            natsConnection.publish(natsSubject, message.getBytes());
            logger.info("Published message to NATS: {}", message);
        } catch (Exception e) {
            logger.error("Failed to publish message to NATS", e);
        }
    }


    private void sendMessageToTelegram(String message) {
        String url = String.format("/bot%s/sendMessage", botToken);
        logger.info("Sending message to " + chatId);

        webClient.post()
                .uri(uriBuilder -> uriBuilder.path(url)
                        .queryParam("chat_id", chatId)
                        .queryParam("text", message)
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .doOnError(error -> logger.error("Error sending message to Telegram: {}", error.getMessage()))
                .subscribe(response -> logger.info("Message sent to Telegram: {}", response));

    }
}

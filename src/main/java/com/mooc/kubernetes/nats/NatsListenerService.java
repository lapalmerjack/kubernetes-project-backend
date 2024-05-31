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

    @Value("${telegram.chat.id")
    private String chatId;


    private final WebClient webClient;

    public NatsListenerService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://api.telegram.org").build();
    }

    @PostConstruct
    public void init() throws IOException, InterruptedException {
        Connection natsConnection = Nats.connect(natsUrl);
        Dispatcher dispatcher = natsConnection.createDispatcher(msg -> {
            String message = new String(msg.getData());
            sendMessageToTelegram(message);
        });
        dispatcher.subscribe(natsSubject);
    }

    private void sendMessageToTelegram(String message) {
        String url = String.format("/bot%s/sendMessage", botToken);

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

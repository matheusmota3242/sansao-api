package dev.m2g2.service;

import dev.m2g2.context.ContextOrchestrator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class TelegramBot extends TelegramLongPollingBot {

    private static final Logger logger = LoggerFactory.getLogger(TelegramBot.class);

    private final String name;

    private final ContextOrchestrator contextOrchestrator;

    public TelegramBot(String name,
                       String token,
                       ContextOrchestrator contextOrchestrator) {
        super(token);
        this.name = name;
        this.contextOrchestrator = contextOrchestrator;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Message message = update.getMessage();
            logger.info("Message received: {}", message);
            try {
                String response;
                response = contextOrchestrator.getResponseMessage(message.getText());
                if (response != null) {
                    execute(new SendMessage(message.getChatId().toString(), response));
                }
            } catch (TelegramApiException e) {
                logger.error("Exception caught: {}", e.getMessage());
            }
        }
    }

    @Override
    public String getBotUsername() {
        return name;
    }
}

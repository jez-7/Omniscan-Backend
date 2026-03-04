package com.monitoreo.bot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Component
public class OmniscanTelegramBot extends TelegramLongPollingBot {

    @Value("${telegram.bot.username}")
    private String botUsername;

    public OmniscanTelegramBot(@Value("${telegram.bot.token}") String botToken) {
        super(botToken);
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public void onUpdateReceived(Update update) {
        // verify if the update has a message and the message has text
        if (update.hasMessage() && update.getMessage().hasText()) {

            String receivedMessage = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();
            String userName = update.getMessage().getFrom().getFirstName();

            log.info("Mensaje recibido de {}: {} (Chat ID: {})", userName, receivedMessage, chatId);

            answer(chatId, "Hola " + userName + ", recibí tu mensaje: '" + receivedMessage + "'");

            // if (mensajeRecibido.startsWith("/track")) { ... }
        }
    }

    // utilty method to send messages back to the user
    private void answer(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);

        try {
            execute(message); // sends the message to telegram
        } catch (TelegramApiException e) {
            log.error("Error al enviar mensaje a Telegram: ", e);
        }
    }
}
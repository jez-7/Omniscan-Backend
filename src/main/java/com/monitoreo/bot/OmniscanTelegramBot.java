package com.monitoreo.bot;

import com.monitoreo.model.entity.Subscription;
import com.monitoreo.repository.SubscriptionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDateTime;

@Slf4j
@Component
public class OmniscanTelegramBot extends TelegramLongPollingBot {

    @Value("${telegram.bot.username}")
    private String botUsername;
    private final SubscriptionRepository subscriptionRepository;

    public OmniscanTelegramBot(@Value("${telegram.bot.token}") String botToken,
                               SubscriptionRepository subscriptionRepository) {
        super(botToken);
        this.subscriptionRepository = subscriptionRepository;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String receivedMessage = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();


            if (receivedMessage.startsWith("/track ")) {
                // extrae todo lo que viene después de /track
                String keyword = receivedMessage.substring(7).trim().toLowerCase();

                Subscription sub = Subscription.builder()
                        .chatId(chatId)
                        .keyword(keyword)
                        .createdAt(LocalDateTime.now())
                        .build();
                subscriptionRepository.save(sub);

                answer(chatId, "✅ ¡Listo! Te avisaré cuando encuentre ofertas para: *" + keyword + "*");
                log.info("Nueva suscripción de Chat ID {}: {}", chatId, keyword);

            } else if (receivedMessage.equals("/start")) {
                answer(chatId, "¡Hola! Soy OmniscanBot 🤖.\nEnviame `/track <producto>` para avisarte de las mejores ofertas.\nEjemplo: `/track asus vivobook`");
            } else {
                answer(chatId, "No entendí ese comando. Probá con `/track <producto>`.");
            }
        }
    }

    private void answer(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);
        message.setParseMode("Markdown");

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error al enviar mensaje a Telegram: ", e);
        }
    }
}
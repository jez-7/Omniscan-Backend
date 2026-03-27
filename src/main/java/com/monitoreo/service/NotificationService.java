package com.monitoreo.service;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Service responsible for sending Telegram notifications when deals are detected.
 * Uses the Telegram API to send messages via a bot, formatted with product details.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${telegram.bot.token}")
    private String botToken;

    /**
     * Sends a Telegram alert with details of the product that dropped in price.
     *
     * @param chatId      Telegram chat ID to notify.
     * @param productName Name of the product.
     * @param price       Current price of the product.
     * @param link        Link to the product in the store.
     */
    public void sendTelegramAlert(Long chatId, String productName, Double price, String link) {
        String text = String.format(
                "🚀 *DEAL DETECTED!* 🚀\n\n" +
                        "📦 *Product:* %s\n" +
                        "💰 *Price:* $%.2f\n\n" +
                        "🔗 [View in store](%s)",
                productName, price, link
        );

        String url = String.format(
                "https://api.telegram.org/bot%s/sendMessage?chat_id=%s&text=%s&parse_mode=Markdown",
                botToken, chatId, text
        );

        try {
            restTemplate.getForObject(url, String.class);
            log.info("Notification sent to chat {} for product: {}", chatId, productName);
        } catch (Exception e) {
            log.error("Error sending Telegram notification: {}", e.getMessage());
        }
    }
}
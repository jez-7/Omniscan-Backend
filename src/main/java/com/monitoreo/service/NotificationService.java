package com.monitoreo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${telegram.bot.token}")
    private String BOT_TOKEN;

    @Value("${telegram.chat.id}")
    private String CHAT_ID;

    public void sendTelegramAlert(String productName, Double price, String link) {
        String text = String.format(
                "🚀 *¡OFERTA DETECTADA!* 🚀\n\n" +
                        "📦 *Producto:* %s\n" +
                        "💰 *Precio:* $%.2f\n\n" +
                        "🔗 [Ver en la tienda](%s)",
                productName, price, link
        );

        String url = String.format(
                "https://api.telegram.org/bot%s/sendMessage?chat_id=%s&text=%s&parse_mode=Markdown",
                BOT_TOKEN, CHAT_ID, text
        );

        try {
            restTemplate.getForObject(url, String.class);
            log.info("Notificación enviada a Telegram para el producto: {}", productName);
        } catch (Exception e) {
            log.error("Error al enviar notificación a Telegram: {}", e.getMessage());
        }


    }

}

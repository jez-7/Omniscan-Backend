package com.monitoreo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class NotificationService {

    private final RestTemplate restTemplate = new RestTemplate();

    private final String BOT_TOKEN = "8472322355:AAFBB05KV7zLnvuXpfAdNaUo0PtyDFvt3MI";
    private final String CHAT_ID = "7840114804";

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
            log.info("✅ Notificación enviada a Telegram para el producto: {}", productName);
        } catch (Exception e) {
            log.error("❌ Error al enviar notificación a Telegram: {}", e.getMessage());
        }


    }

}

package com.monitoreo.service;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Servicio encargado de enviar notificaciones a Telegram cuando se detectan ofertas.
 * Utiliza la API de Telegram para enviar mensajes mediante un bot, formateados con detalles del producto.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${telegram.bot.token}")
    private String BOT_TOKEN;

    @Value("${telegram.chat.id}")
    private String CHAT_ID;

    /**
     * Envía una alerta a Telegram con los detalles del producto que ha bajado de precio.
     *
     * @param productName Nombre del producto.
     * @param price       Precio actual del producto.
     * @param link        Enlace al producto en la tienda.
     */
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

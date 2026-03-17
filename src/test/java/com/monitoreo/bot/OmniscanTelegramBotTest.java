package com.monitoreo.bot;

import com.monitoreo.model.entity.Subscription;
import com.monitoreo.repository.SubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OmniscanTelegramBotTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    private OmniscanTelegramBot bot;

    @BeforeEach
    void setUp() {
        bot = spy(new OmniscanTelegramBot("fake-token", subscriptionRepository));
    }

    @Test
    void testGetBotUsername() {
        assertNull(bot.getBotUsername());
    }

    @Test
    void testOnUpdateReceived_trackCommand() throws Exception {
        Update update = createUpdate("/track laptop asus", 12345L);

        doReturn(null).when(bot).execute(any(SendMessage.class));

        bot.onUpdateReceived(update);

        verify(subscriptionRepository, times(1)).save(any(Subscription.class));
    }

    @Test
    void testOnUpdateReceived_startCommand() throws Exception {
        Update update = createUpdate("/start", 12345L);

        doReturn(null).when(bot).execute(any(SendMessage.class));

        bot.onUpdateReceived(update);

        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    void testOnUpdateReceived_unknownCommand() throws Exception {
        Update update = createUpdate("hola", 12345L);

        doReturn(null).when(bot).execute(any(SendMessage.class));

        bot.onUpdateReceived(update);

        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    void testOnUpdateReceived_noMessage() {
        Update update = new Update();

        bot.onUpdateReceived(update);

        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    void testOnUpdateReceived_messageWithoutText() {
        Update update = new Update();
        Message message = new Message();
        update.setMessage(message);

        bot.onUpdateReceived(update);

        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    void testOnUpdateReceived_answerThrowsException() throws Exception {
        Update update = createUpdate("/start", 99999L);

        doThrow(new TelegramApiException("API error"))
                .when(bot).execute(any(SendMessage.class));

        bot.onUpdateReceived(update);
    }

    private Update createUpdate(String text, Long chatId) {
        Update update = new Update();
        Message message = new Message();
        message.setText(text);

        Chat chat = new Chat();
        chat.setId(chatId);
        message.setChat(chat);

        update.setMessage(message);
        return update;
    }
}

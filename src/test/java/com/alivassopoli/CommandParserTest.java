package com.alivassopoli;

import com.alivassopoli.adapter.telegram.TelegramMessageCommandSender;
import com.alivassopoli.security.Policy;
import com.alivassopoli.security.Role;
import com.alivassopoli.service.VassopoliService;
import com.alivassopoli.service.VassopoliServiceFactory;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import javax.inject.Inject;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@QuarkusTest
public class CommandParserTest {

    @Inject
    CommandParser commandParser;

    @InjectMock
    VassopoliServiceFactory vassopoliServiceFactory;

    @InjectMock
    TelegramMessageCommandSender telegramMessageCommandSender;

    private Update mockUpdate;
    private Message mockMessage;
    private VassopoliService mockService;

    @BeforeEach
    void setUp() {
        commandParser.clearCache();
        mockUpdate = Mockito.mock(Update.class);
        mockMessage = Mockito.mock(Message.class);
        mockService = Mockito.mock(VassopoliService.class);
        Chat mockChat = Mockito.mock(Chat.class);
        User mockUser = Mockito.mock(User.class);

        Mockito.when(mockUpdate.getMessage()).thenReturn(mockMessage);
        Mockito.when(mockMessage.getText()).thenReturn("/test");
        Mockito.when(mockMessage.getChatId()).thenReturn(123L);
        Mockito.when(mockMessage.getMessageId()).thenReturn(456);
        Mockito.when(mockMessage.getFrom()).thenReturn(mockUser);
        Mockito.when(mockUser.getUserName()).thenReturn("testuser");
        Mockito.when(mockMessage.getChat()).thenReturn(mockChat);
        Mockito.when(mockChat.getId()).thenReturn(123L);

        Mockito.when(vassopoliServiceFactory.execute(anyString())).thenReturn(mockService);
    }

    @Test
    void testExecute_firstTime_userAllowed() {
        Role allowedRole = Role.ADMIN;
        Mockito.when(mockService.getRequiredPolicy()).thenReturn(Policy.LLM_USER);
        Message sentMessage = Mockito.mock(Message.class);
        Mockito.when(sentMessage.getChatId()).thenReturn(123L);
        Mockito.when(sentMessage.getMessageId()).thenReturn(789);
        Mockito.when(telegramMessageCommandSender.executeSend(any(), any(), anyString())).thenReturn(Optional.of(sentMessage));

        commandParser.execute(mockUpdate, allowedRole);

        Mockito.verify(vassopoliServiceFactory).execute("/test");
        Mockito.verify(mockService).execute(mockUpdate);
        Mockito.verify(telegramMessageCommandSender).executeSend(456, "123", "Received!");
        Mockito.verify(telegramMessageCommandSender).executeDelete("123", 789);
    }

    @Test
    void testExecute_secondTime_userAllowed() {
        Role allowedRole = Role.ADMIN;
        Mockito.when(mockService.getRequiredPolicy()).thenReturn(Policy.LLM_USER);
        Message sentMessage = Mockito.mock(Message.class);
        Mockito.when(sentMessage.getChatId()).thenReturn(123L);
        Mockito.when(sentMessage.getMessageId()).thenReturn(789);
        Mockito.when(telegramMessageCommandSender.executeSend(any(), any(), anyString())).thenReturn(Optional.of(sentMessage));


        // First execution
        commandParser.execute(mockUpdate, allowedRole);
        // Second execution
        commandParser.execute(mockUpdate, allowedRole);

        Mockito.verify(vassopoliServiceFactory, times(2)).execute("/test");
        Mockito.verify(mockService, times(2)).execute(mockUpdate);
        Mockito.verify(telegramMessageCommandSender, times(1)).executeSend(any(), any(), anyString());
        Mockito.verify(telegramMessageCommandSender, times(1)).executeDelete(anyString(), any());
    }

    @Test
    void testExecute_userNotAllowed() {
        Role notAllowedRole = Role.USER;
        Mockito.when(mockService.getRequiredPolicy()).thenReturn(Policy.LLM_USER);

        commandParser.execute(mockUpdate, notAllowedRole);

        Mockito.verify(vassopoliServiceFactory).execute("/test");
        Mockito.verify(mockService, never()).execute(any(Update.class));
        Mockito.verify(telegramMessageCommandSender, never()).executeSend(any(), any(), anyString());
        Mockito.verify(telegramMessageCommandSender, never()).executeDelete(anyString(), any());
    }
}

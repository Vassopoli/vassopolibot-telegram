package com.alivassopoli.adapter.telegram;

import org.telegram.telegrambots.meta.api.methods.ForwardMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.enterprise.context.ApplicationScoped;
import java.util.Optional;

@ApplicationScoped
public class TelegramMessageCommandSender {

    private final VassopoliBotTelegramWebhook vassopoliBotTelegramWebHook;

    public TelegramMessageCommandSender(final VassopoliBotTelegramWebhook vassopoliBotTelegramWebHook) {
        this.vassopoliBotTelegramWebHook = vassopoliBotTelegramWebHook;
    }

    public void executeSend(final String receiver, final String textMessage) {
        executeSend(receiver, textMessage, false);
    }

    public void executeSend(final String receiver, final String textMessage, final boolean enableMarkdownV2) {
        final SendMessage sendMessage = SendMessage.builder()
                .chatId(receiver)
                .text(textMessage)
                .build();

        sendMessage.enableMarkdownV2(enableMarkdownV2);

        try {
            vassopoliBotTelegramWebHook.execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public Optional<Message> executeSend(final Integer replyToMessage, final String receiver, final String textMessage) {
        return executeSend(replyToMessage, receiver, textMessage, false);
    }

    //TODO: refactor
    public Optional<Message> executeSend(final Integer replyToMessage, final String receiver, final String textMessage, final boolean enableMarkdownV2) {
        final SendMessage sendMessage = SendMessage.builder()
                .replyToMessageId(replyToMessage)
                .chatId(receiver)
                .text(textMessage)
                .build();

        sendMessage.enableMarkdownV2(enableMarkdownV2);

        try {
            return Optional.of(vassopoliBotTelegramWebHook.execute(sendMessage));
        } catch (TelegramApiException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public Optional<Message> executeForward(final String receiver, final Long fromChatId, final Integer messageId) {
        final ForwardMessage forwardMessage = ForwardMessage.builder()
                .chatId(receiver)
                .fromChatId(fromChatId)
                .messageId(messageId)
                .build();

        try {
            return Optional.of(vassopoliBotTelegramWebHook.execute(forwardMessage));
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    public Optional<Boolean> executeDelete(final String chatId, final Integer messageId) {
        final DeleteMessage deleteMessage = DeleteMessage.builder()
                .chatId(chatId)
                .messageId(messageId)
                .build();

        try {
            return Optional.of(vassopoliBotTelegramWebHook.execute(deleteMessage));
        } catch (TelegramApiException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }
}

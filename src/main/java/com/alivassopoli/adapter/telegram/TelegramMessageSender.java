package com.alivassopoli.adapter.telegram;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.enterprise.context.ApplicationScoped;
import java.util.Optional;

@ApplicationScoped
public class TelegramMessageSender {

    private final VassopoliBotTelegramWebhook vassopoliBotTelegramWebHook;

    public TelegramMessageSender(final VassopoliBotTelegramWebhook vassopoliBotTelegramWebHook) {
        this.vassopoliBotTelegramWebHook = vassopoliBotTelegramWebHook;
    }

    public void execute(final String receiver, final String textMessage) {
        execute(receiver, textMessage, false);
    }

    public void execute(final String receiver, final String textMessage, final boolean enableMarkdownV2) {
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

    public Optional<Message> execute(final Integer replyToMessage, final String receiver, final String textMessage) {
        return execute(replyToMessage, receiver, textMessage, false);
    }

    //TODO: refactor
    public Optional<Message> execute(final Integer replyToMessage, final String receiver, final String textMessage, final boolean enableMarkdownV2) {
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

    public Optional<Boolean> execute(final String chatId, final Integer messageId) {
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

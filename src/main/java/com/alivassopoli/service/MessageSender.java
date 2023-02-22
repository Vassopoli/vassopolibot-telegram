package com.alivassopoli.service;

import com.alivassopoli.adapter.telegram.TelegramMessageCommandSender;
import com.alivassopoli.security.Policy;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class MessageSender implements VassopoliService {
    private static final Logger LOG = Logger.getLogger(MessageSender.class);

    private final Long vassopoliID;
    private final Long vassopoliBackupID;
    private final TelegramMessageCommandSender telegramMessageCommandSender;

    public MessageSender(
            @ConfigProperty(name = "vassopolibot-telegram-webhook.telegram.vassopoli-id") final Long vassopoliID,
            @ConfigProperty(name = "vassopolibot-telegram-webhook.telegram.vassopoli-backup-id") final Long vassopoliBackupID,
            final TelegramMessageCommandSender telegramMessageCommandSender) {
        this.vassopoliID = vassopoliID;
        this.vassopoliBackupID = vassopoliBackupID;
        this.telegramMessageCommandSender = telegramMessageCommandSender;
    }

    @Override
    public Policy getRequiredPolicy() {
        return Policy.MESSAGE_SENDER;
    }

    @Override
    public List<String> getCommand() {
        return List.of("message");
    }

    @Override
    public void execute(final Update update) {
        final String messageSanitized = update.getMessage().getText().toLowerCase().replace("message ", "");

        if (isMessageFromVassopoli((update.getMessage()))) {
            LOG.info("Vassopoli sending message to someone");

            if (update.getMessage().isReply()) {
                // Replies doest need to get the receiver from message command

                final Message messageReplied = update.getMessage().getReplyToMessage();
                final Long forwardFromUserId = messageReplied.getForwardFrom().getId();

                 telegramMessageCommandSender.executeSend(forwardFromUserId.toString(), messageSanitized);

                telegramMessageCommandSender.executeSend(update.getMessage().getMessageId(), update.getMessage().getChatId().toString(),
                        "\uD83E\uDD16 Message sent!");

            } else {
                // Otherwise, it needs the receiver on the message command
                throw new UnsupportedOperationException("Not implemented yet");
            }

        } else {
            LOG.info("Someone sending message to Vassopoli");

            telegramMessageCommandSender.executeForward(String.valueOf(vassopoliID), update.getMessage().getChatId(), update.getMessage().getMessageId());
            telegramMessageCommandSender.executeForward(String.valueOf(vassopoliBackupID), update.getMessage().getChatId(), update.getMessage().getMessageId());

            telegramMessageCommandSender.executeSend(update.getMessage().getMessageId(), update.getMessage().getChatId().toString(),
                    "\uD83E\uDD16 Message sent!");
        }
    }

    private boolean isMessageFromVassopoli(final Message message) {
        return message.getFrom().getId().equals(vassopoliID) || message.getFrom().getId().equals(vassopoliBackupID);
    }
}

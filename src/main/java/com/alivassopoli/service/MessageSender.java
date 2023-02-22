package com.alivassopoli.service;

import com.alivassopoli.adapter.telegram.TelegramMessageCommandSender;
import com.alivassopoli.security.Policy;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

            if (update.getMessage().isReply()) {
                // Replies doest need to know the receiver

                final String messageReplied = update.getMessage().getReplyToMessage().getText();
                final Pattern p = Pattern.compile("chatId: (.*)");
                final Matcher m = p.matcher(messageReplied);

                if (m.find()){
                    final String chatId = m.group(1);
                    LOG.infof("chatId %s found on message replied", chatId);
                    telegramMessageCommandSender.executeSend(chatId, messageSanitized);

                    telegramMessageCommandSender.executeSend(update.getMessage().getMessageId(), update.getMessage().getChatId().toString(),
                            "\uD83E\uDD16 Message sent!");
                } else {
                    LOG.errorf("chatId not found on message %s", messageReplied);
                }


            } else {
                // Otherwise, it needs the receiver
            }

        } else {
            final String messageWithAdministrativeDetails = "Message from\n\n"
                    + "chatId: " + update.getMessage().getChatId() + "\n"
                    + "chatName: " + update.getMessage().getChat().getTitle() + "\n"
                    + "userId: " + update.getMessage().getFrom().getId() + "\n"
                    + "databaseUserNameAlias: " + null + "\n"
                    + "userName: " + update.getMessage().getFrom().getUserName() + "\n"
                    + "firstName: " + update.getMessage().getFrom().getFirstName() + "\n\n"
                    + messageSanitized;

            telegramMessageCommandSender.executeSend(String.valueOf(vassopoliID), messageWithAdministrativeDetails);
            telegramMessageCommandSender.executeSend(String.valueOf(vassopoliBackupID), messageWithAdministrativeDetails);

            telegramMessageCommandSender.executeSend(update.getMessage().getMessageId(), update.getMessage().getChatId().toString(),
                    "\uD83E\uDD16 Message sent!");
        }
    }

    private boolean isMessageFromVassopoli(final Message message) {
        return message.getFrom().getId().equals(vassopoliID) || message.getFrom().getId().equals(vassopoliBackupID);
    }
}

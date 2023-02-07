package com.alivassopoli.service;

import com.alivassopoli.adapter.telegram.TelegramMessageCommandSender;
import com.alivassopoli.security.Role;
import com.alivassopoli.util.GetTelegramSenderName;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.telegram.telegrambots.meta.api.objects.Update;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class MessageSender implements VassopoliService {

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
    public Role getRequiredRole() {
        return Role.USER;
    }

    @Override
    public List<String> getCommand() {
        return List.of("message vassopoli");
    }

    @Override
    public void execute(final Update update) {
        final String messageSanitized = update.getMessage().getText().toLowerCase().replace("message vassopoli ", "");
        final String finalMessage = "Message from " + GetTelegramSenderName.execute(update.getMessage()) + ":\n\n" + messageSanitized;

        telegramMessageCommandSender.executeSend(String.valueOf(vassopoliID), finalMessage);
        telegramMessageCommandSender.executeSend(String.valueOf(vassopoliBackupID), finalMessage);

        telegramMessageCommandSender.executeSend(update.getMessage().getMessageId(), update.getMessage().getChatId().toString(),
                "Message sent!");
    }
}

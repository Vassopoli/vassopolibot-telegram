package com.alivassopoli.adapter.telegram;

import com.alivassopoli.CommandParser;
import com.alivassopoli.configuration.MyURLs;
import com.alivassopoli.security.Role;
import com.alivassopoli.security.UserAuthenticator;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

import javax.enterprise.context.ApplicationScoped;
import java.util.Objects;

@ApplicationScoped
public class VassopoliBotTelegramWebhook extends TelegramWebhookBot {
    private static final Logger LOG = Logger.getLogger(VassopoliBotTelegramWebhook.class);

    private final String token;
    private final UserAuthenticator userAuthenticator;
    private final CommandParser commandParser;
    private final TelegramMessageCommandSender telegramMessageCommandSender;
    private final Long vassopoliID;
    private final Long vassopoliBackupID;

    public VassopoliBotTelegramWebhook(@ConfigProperty(name = "vassopolibot-telegram-webhook.telegram.token") final String token,
                                       final UserAuthenticator userAuthenticator, final CommandParser commandParser,
                                       final TelegramMessageCommandSender telegramMessageCommandSender,
                                       @ConfigProperty(name = "vassopolibot-telegram-webhook.telegram.vassopoli-id") final Long vassopoliID,
                                       @ConfigProperty(name = "vassopolibot-telegram-webhook.telegram.vassopoli-backup-id") final Long vassopoliBackupID) {
        this.token = token;
        this.userAuthenticator = userAuthenticator;
        this.commandParser = commandParser;
        this.telegramMessageCommandSender = telegramMessageCommandSender;
        this.vassopoliID = vassopoliID;
        this.vassopoliBackupID = vassopoliBackupID;
    }

    @Override
    public String getBotUsername() {
        return "VassopoliBot";
    }

    @Override
    public String getBotToken() {
        return token;
    }

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(final Update update) {
        try {
            //TODO: create a flow for forbidden users, null updates, updates without a message, and messages without text (THE ELSE OF THIS IF)

            LOG.info("Some message received");
            LOG.info(update);

            if (isProcessableMessage(update)) {
                final Role role = userAuthenticator.getChatRole(update.getMessage().getChatId());
                LOG.infof("Message from %s of role %s", update.getMessage().getFrom().getUserName(), role.toString());
                LOG.info(update.getMessage());

                if (update.getMessage().hasPhoto()) {
                    LOG.info("Photo is being processed");
                    telegramMessageCommandSender.executeForward(String.valueOf(vassopoliID), update.getMessage().getChatId(), update.getMessage().getMessageId());
                    telegramMessageCommandSender.executeForward(String.valueOf(vassopoliBackupID), update.getMessage().getChatId(), update.getMessage().getMessageId());

                } else if (update.getMessage().hasText()) {
                    LOG.info("Text is being processed");
                    //Even if it's a photo with caption, will not enter here

                    //TODO: Send to command-parser lambda
                    // or use strategy and extract user intention in functional way, with return
                    commandParser.execute(update, role);
                }
            } else {
                LOG.warn("Message was no processed properly because it's not processable");
                telegramMessageCommandSender.executeSend(String.valueOf(vassopoliID), "Message was no processed properly because it's not processable");
            }
        } catch (Exception ex) {
            LOG.error("Message was no processed properly due to an exception", ex);
            telegramMessageCommandSender.executeSend(String.valueOf(vassopoliID), "Message was no processed properly due to an exception: " + ex.getMessage());
        }
        return null;
    }

    @Override
    public String getBotPath() {
        return MyURLs.RANDOM_TOKEN_FOR_SECURITY_PURPOSE;
    }

    private boolean isProcessableMessage(Update update) {
        return Objects.nonNull(update) && update.hasMessage();
    }
}

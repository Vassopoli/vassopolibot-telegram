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

    public VassopoliBotTelegramWebhook(@ConfigProperty(name = "vassopolibot-telegram-webhook.telegram.token") final String token,
                                       final UserAuthenticator userAuthenticator, final CommandParser commandParser) {
        this.token = token;
        this.userAuthenticator = userAuthenticator;
        this.commandParser = commandParser;
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
        //TODO: create a flow for forbidden users, null updates, updates without a message, and messages without text (THE ELSE OF THIS IF)
        if (isProcessableMessage(update)) {
            final Role role = userAuthenticator.getChatRole(update.getMessage().getChatId());
            LOG.infof("Message from %s %s received", role.toString(), update.getMessage().getFrom().getUserName());
            LOG.info(update.getMessage());

            //TODO: Send to command-parser lambda
            // or use strategy and extract user intention in functional way, with return
            commandParser.execute(update);
        }
        return null;
    }

    @Override
    public String getBotPath() {
        return MyURLs.RANDOM_TOKEN_FOR_SECURITY_PURPOSE;
    }

    private boolean isProcessableMessage(Update update) {
        return Objects.nonNull(update) && update.hasMessage() && update.getMessage().hasText();
    }
}

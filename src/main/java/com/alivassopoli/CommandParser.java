package com.alivassopoli;

import com.alivassopoli.adapter.telegram.TelegramMessageCommandSender;
import com.alivassopoli.security.Role;
import com.alivassopoli.security.UserAuthenticator;
import com.alivassopoli.service.VassopoliService;
import com.alivassopoli.service.VassopoliServiceFactory;
import io.quarkus.logging.Log;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import javax.enterprise.context.ApplicationScoped;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@ApplicationScoped
public class CommandParser {
    private static final Logger LOG = Logger.getLogger(CommandParser.class);

    private final Set<String> invocationCache = new HashSet<>();
    private final VassopoliServiceFactory vassopoliServiceFactory;
    private final UserAuthenticator userAuthenticator;
    private final TelegramMessageCommandSender telegramMessageCommandSender;
    private final Long vassopoliID;

    public CommandParser(final VassopoliServiceFactory vassopoliServiceFactory, final UserAuthenticator userAuthenticator,
                         final TelegramMessageCommandSender telegramMessageCommandSender,
                         @ConfigProperty(name = "vassopolibot-telegram-webhook.telegram.vassopoli-id") final Long vassopoliID) {
        this.vassopoliServiceFactory = vassopoliServiceFactory;
        this.userAuthenticator = userAuthenticator;
        this.telegramMessageCommandSender = telegramMessageCommandSender;
        this.vassopoliID = vassopoliID;
    }

    public void execute(final Update update) {
        final Optional<VassopoliService> vassopoliServiceOptional = vassopoliServiceFactory.execute(update.getMessage().getText());

        final Role userRole = userAuthenticator.getChatRole(update.getMessage().getChatId());

        vassopoliServiceOptional.ifPresentOrElse(vassopoliService -> {
            if (userRole.getCode() >= vassopoliService.getRequiredRole().getCode()) {

                final String cacheKey = update.getMessage().getChatId().toString() + vassopoliService.getClass().getSimpleName();
                final boolean wasFirstTimeAddedToCache = invocationCache.add(cacheKey);

                final Optional<Message> receivedMessageOptional;

                if (wasFirstTimeAddedToCache) {
                    Log.infof("cacheKey %s created", cacheKey);
                    receivedMessageOptional = telegramMessageCommandSender.executeSend(update.getMessage().getMessageId(), update.getMessage().getChatId().toString(), "Received!");
                } else {
                    Log.infof("cacheKey %s already exists", cacheKey);
                    receivedMessageOptional = Optional.empty();
                }

                LOG.infof("%s %s performing %s %s", userRole, update.getMessage().getFrom().getUserName(), vassopoliService.getRequiredRole(), vassopoliService.getClass().getSimpleName());
                vassopoliService.execute(update);

                receivedMessageOptional.ifPresent(m -> telegramMessageCommandSender.executeDelete(m.getChatId().toString(), m.getMessageId()));

            } else {
                LOG.infof("%s %s not allowed to perform %s %s", userRole, update.getMessage().getFrom().getUserName(), vassopoliService.getRequiredRole(), vassopoliService.getRequiredRole());
            }
        }, () -> {
            if (!Role.UNKNOWN.equals(userRole)) {
                telegramMessageCommandSender.executeSend(String.valueOf(vassopoliID), "Intention of message \"" + update.getMessage().getText() + "\" from chat " + getChatTitleOrUsernameOrFirstName(update.getMessage().getChat()) + " was not recognized!");
            }
        });
    }

    private String getChatTitleOrUsernameOrFirstName(final Chat chat) {
        //TODO: Refactor, and use across application when a username is needed
        //TODO: on top of all, get alias from database of roles
        return Objects.requireNonNullElse(
                chat.getTitle(), Objects.requireNonNullElse(chat.getUserName(), chat.getFirstName()));
    }
}

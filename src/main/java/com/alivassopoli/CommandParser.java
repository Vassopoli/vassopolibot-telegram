package com.alivassopoli;

import com.alivassopoli.adapter.telegram.TelegramMessageSender;
import com.alivassopoli.security.Role;
import com.alivassopoli.security.UserAuthenticator;
import com.alivassopoli.service.VassopoliService;
import com.alivassopoli.service.VassopoliServiceFactory;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import javax.enterprise.context.ApplicationScoped;
import java.util.Objects;
import java.util.Optional;

@ApplicationScoped
public class CommandParser {
    private static final Logger LOG = Logger.getLogger(CommandParser.class);

    private final VassopoliServiceFactory vassopoliServiceFactory;
    private final UserAuthenticator userAuthenticator;
    private final TelegramMessageSender telegramMessageSender;
    private final Long vassopoliID;

    public CommandParser(final VassopoliServiceFactory vassopoliServiceFactory, final UserAuthenticator userAuthenticator,
                         final TelegramMessageSender telegramMessageSender,
                         @ConfigProperty(name = "vassopolibot-telegram-webhook.telegram.vassopoli-id") final Long vassopoliID) {
        this.vassopoliServiceFactory = vassopoliServiceFactory;
        this.userAuthenticator = userAuthenticator;
        this.telegramMessageSender = telegramMessageSender;
        this.vassopoliID = vassopoliID;
    }

    public void execute(final Update update) {
        final Optional<VassopoliService> vassopoliServiceOptional = vassopoliServiceFactory.execute(update.getMessage().getText());

        final Role userRole = userAuthenticator.getChatRole(update.getMessage().getChatId());

        vassopoliServiceOptional.ifPresentOrElse(vassopoliService -> {
            if (userRole.getCode() >= vassopoliService.getRequiredRole().getCode()) {
                final Optional<Message> message = telegramMessageSender.execute(update.getMessage().getMessageId(), update.getMessage().getChatId().toString(), "Received!");

                LOG.infof("%s %s performing %s %s", userRole, update.getMessage().getFrom().getUserName(), vassopoliService.getRequiredRole(), vassopoliService.getClass().getSimpleName());
                vassopoliService.execute(update);
                //Deletes the "received"
                message.ifPresent(m -> telegramMessageSender.execute(m.getChatId().toString(), m.getMessageId()));

            } else {
                LOG.infof("%s %s not allowed to perform %s %s", userRole, update.getMessage().getFrom().getUserName(), vassopoliService.getRequiredRole(), vassopoliService.getRequiredRole());
            }
        }, () -> {
            if (!Role.UNKNOWN.equals(userRole)) {
                telegramMessageSender.execute(String.valueOf(vassopoliID), "Intention of message \"" + update.getMessage().getText() + "\" from chat " + getChatTitleOrUsername(update.getMessage().getChat()) + " was not recognized!");
            }
        });
    }

    private String getChatTitleOrUsername(final Chat chat) {
        return Objects.requireNonNullElse(chat.getTitle(), chat.getUserName());
    }
}

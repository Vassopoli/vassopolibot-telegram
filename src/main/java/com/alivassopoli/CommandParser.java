package com.alivassopoli;

import com.alivassopoli.adapter.telegram.TelegramMessageCommandSender;
import com.alivassopoli.security.Role;
import com.alivassopoli.service.VassopoliService;
import com.alivassopoli.service.VassopoliServiceFactory;
import io.quarkus.logging.Log;
import org.jboss.logging.Logger;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import javax.enterprise.context.ApplicationScoped;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@ApplicationScoped
public class CommandParser {
    private static final Logger LOG = Logger.getLogger(CommandParser.class);

    private final Set<String> invocationCache = new HashSet<>();
    private final VassopoliServiceFactory vassopoliServiceFactory;
    private final TelegramMessageCommandSender telegramMessageCommandSender;

    public CommandParser(final VassopoliServiceFactory vassopoliServiceFactory,
                         final TelegramMessageCommandSender telegramMessageCommandSender) {
        this.vassopoliServiceFactory = vassopoliServiceFactory;
        this.telegramMessageCommandSender = telegramMessageCommandSender;
    }

    public void execute(final Update update, final Role role) {
        final VassopoliService vassopoliService = vassopoliServiceFactory.execute(update.getMessage().getText());

            if (role.getPolicySet().contains(vassopoliService.getRequiredPolicy())) {

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

                LOG.infof("%s of role %s performing service %s of %s policy", update.getMessage().getFrom().getUserName(), role, vassopoliService.getClass().getSimpleName(), vassopoliService.getRequiredPolicy());
                vassopoliService.execute(update);

                receivedMessageOptional.ifPresent(m -> telegramMessageCommandSender.executeDelete(m.getChatId().toString(), m.getMessageId()));

            } else {
                LOG.infof("%s of role %s not allowed to execute service %s of %s policy", update.getMessage().getFrom().getUserName(), role, vassopoliService.getClass().getSimpleName(), vassopoliService.getRequiredPolicy());
            }
    }

    public void clearCache() {
        invocationCache.clear();
    }
}

package com.alivassopoli.security;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import java.util.Map;

@ApplicationScoped
public class UserAuthenticator {

    private final Long vassopoliID;
    private final Long aptoID;

    public UserAuthenticator(@ConfigProperty(name = "vassopolibot-telegram-webhook.telegram.vassopoli-id") final Long vassopoliID,
                             @ConfigProperty(name = "vassopolibot-telegram-webhook.telegram.apto-id") final Long aptoID) {
        this.vassopoliID = vassopoliID;
        this.aptoID = aptoID;
    }

    public Role getChatRole(Long chatID) {
        final Map<Long, Role> idToRole = Map.of(
                vassopoliID, Role.ADMIN,
                aptoID, Role.USER
        );

        return idToRole.getOrDefault(chatID, Role.UNKNOWN);
    }
}

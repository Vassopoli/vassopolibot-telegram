package com.alivassopoli.security;

import io.quarkus.test.junit.QuarkusTestProfile;

import java.util.Map;

public class UserAuthenticatorTestProfile implements QuarkusTestProfile {

    @Override
    public Map<String, String> getConfigOverrides() {
        return Map.of(
                "vassopolibot-telegram-webhook.telegram.vassopoli-id", "123",
                "vassopolibot-telegram-webhook.telegram.vassopoli-backup-id", "456",
                "vassopolibot-telegram-webhook.telegram.apto-id", "789"
        );
    }
}

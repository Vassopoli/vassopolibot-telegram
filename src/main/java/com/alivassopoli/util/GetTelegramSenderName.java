package com.alivassopoli.util;

import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.Objects;

public class GetTelegramSenderName {

    private static final String SIMULATING_NULL_TELEGRAM_SENDER_NAME_FROM_DATABASE = null;

    private GetTelegramSenderName() {}

    public static String execute(final Message message) {
        final String databaseAlias = Objects.isNull(SIMULATING_NULL_TELEGRAM_SENDER_NAME_FROM_DATABASE) ? null : "databaseAlias{" + SIMULATING_NULL_TELEGRAM_SENDER_NAME_FROM_DATABASE + "}";
        final String userName = Objects.isNull(message.getFrom().getUserName()) ? null : "userName{" + message.getFrom().getUserName() + "}";
        final String firstName = Objects.isNull(message.getChat().getFirstName()) ? null : "firstName{" + message.getChat().getFirstName() + "}";

        return Objects.requireNonNullElse(databaseAlias,
                Objects.requireNonNullElse(userName, firstName));
    }
}

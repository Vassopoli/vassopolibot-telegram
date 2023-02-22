package com.alivassopoli.configuration;

public final class MyURLs {

    private MyURLs() {}

    public static final String RANDOM_TOKEN_FOR_SECURITY_PURPOSE = "65c54aed-b0c6-4963-93a4-72df5ddfd3a1";

    public static final String TELEGRAM_REGISTER_WEBHOOK_URL = "/register-webhook/" + RANDOM_TOKEN_FOR_SECURITY_PURPOSE;
    public static final String TELEGRAM_REGISTER_COMMANDS_URL = "/register-commands/" + RANDOM_TOKEN_FOR_SECURITY_PURPOSE;
    public static final String TELEGRAM_CALLBACK_URL = "/callback/" + RANDOM_TOKEN_FOR_SECURITY_PURPOSE;
}

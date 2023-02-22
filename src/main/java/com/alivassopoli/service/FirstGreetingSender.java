package com.alivassopoli.service;

import com.alivassopoli.adapter.telegram.TelegramMessageCommandSender;
import com.alivassopoli.security.Policy;
import org.telegram.telegrambots.meta.api.objects.Update;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class FirstGreetingSender implements VassopoliService {

    private final TelegramMessageCommandSender telegramMessageCommandSender;

    public FirstGreetingSender(final TelegramMessageCommandSender telegramMessageCommandSender) {
        this.telegramMessageCommandSender = telegramMessageCommandSender;
    }

    @Override
    public Policy getRequiredPolicy() {
        return Policy.FIRST_GREETING_SENDER;
    }

    @Override
    public List<String> getCommand() {
        return List.of("/start");
    }

    @Override
    public void execute(Update update) {
        telegramMessageCommandSender.executeSend(update.getMessage().getMessageId(), update.getMessage().getChatId().toString(), "Welcome to vassopoli bot! Here is a list of what this bot can do for you...");
        telegramMessageCommandSender.executeSend(update.getMessage().getChatId().toString(), "TBA"); //TODO: Return dynamically
    }
}

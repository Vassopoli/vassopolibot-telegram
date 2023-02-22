package com.alivassopoli.service;

import com.alivassopoli.adapter.telegram.TelegramMessageCommandSender;
import com.alivassopoli.security.Policy;
import org.telegram.telegrambots.meta.api.objects.Update;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class InstagramSaver implements VassopoliService {

    private final TelegramMessageCommandSender telegramMessageCommandSender;

    public InstagramSaver(final TelegramMessageCommandSender telegramMessageCommandSender) {
        this.telegramMessageCommandSender = telegramMessageCommandSender;
    }

    @Override
    public Policy getRequiredPolicy() {
        return Policy.INSTAGRAM_SAVER;
    }

    @Override
    public List<String> getCommand() {
        return List.of("https://www.instagram.com");
    }

    @Override
    public void execute(Update update) {
        telegramMessageCommandSender.executeSend(update.getMessage().getMessageId(), update.getMessage().getChatId().toString(), update.getMessage().getText());
    }
}

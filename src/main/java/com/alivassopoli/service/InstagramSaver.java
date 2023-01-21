package com.alivassopoli.service;

import com.alivassopoli.adapter.telegram.TelegramMessageSender;
import com.alivassopoli.security.Role;
import org.telegram.telegrambots.meta.api.objects.Update;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class InstagramSaver implements VassopoliService {

    private final TelegramMessageSender telegramMessageSender;

    public InstagramSaver(final TelegramMessageSender telegramMessageSender) {
        this.telegramMessageSender = telegramMessageSender;
    }

    @Override
    public Role getRequiredRole() {
        return Role.ADMIN;
    }

    @Override
    public List<String> getCommand() {
        return List.of("https://www.instagram.com");
    }

    @Override
    public void execute(Update update) {
        telegramMessageSender.execute(update.getMessage().getMessageId(), update.getMessage().getChatId().toString(), update.getMessage().getText());
    }
}

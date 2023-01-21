package com.alivassopoli.service;

import com.alivassopoli.adapter.dynamodb.ShoppingListRepository;
import com.alivassopoli.adapter.telegram.TelegramMessageSender;
import com.alivassopoli.security.Role;
import io.quarkus.logging.Log;
import org.telegram.telegrambots.meta.api.objects.Update;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemResponse;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class ShoppingListDeleter implements VassopoliService {

    private final ShoppingListRepository shoppingListRepository;
    private final TelegramMessageSender telegramMessageSender;

    public ShoppingListDeleter(final ShoppingListRepository shoppingListRepository, final TelegramMessageSender telegramMessageSender) {
        this.shoppingListRepository = shoppingListRepository;
        this.telegramMessageSender = telegramMessageSender;
    }

    @Override
    public Role getRequiredRole() {
        return Role.USER;
    }

    @Override
    public List<String> getCommand() {
        return List.of("market delete", "market remove");
    }

    @Override
    public void execute(Update update) {
        final String messageSanitized = update.getMessage().getText().toLowerCase().replace("market delete ", "").replace("market remove ", "").strip();
        final DeleteItemResponse deleteItemResponse = shoppingListRepository.delete(messageSanitized);
        Log.infof("deleteItemResponse: %s", deleteItemResponse);

        final String message;
        if (deleteItemResponse.attributes().isEmpty()) {
            message = messageSanitized + " not found!";
        } else {
            message = messageSanitized + " deleted!";
        }

        telegramMessageSender.execute(update.getMessage().getMessageId(), update.getMessage().getChatId().toString(), message);
    }
}

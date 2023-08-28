package com.alivassopoli.service;

import com.alivassopoli.adapter.dynamodb.ShoppingListRepository;
import com.alivassopoli.adapter.telegram.TelegramMessageCommandSender;
import com.alivassopoli.security.Policy;
import io.quarkus.logging.Log;
import org.telegram.telegrambots.meta.api.objects.Update;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemResponse;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class ShoppingListDeleter implements VassopoliService {

    private final ShoppingListRepository shoppingListRepository;
    private final TelegramMessageCommandSender telegramMessageCommandSender;

    public ShoppingListDeleter(final ShoppingListRepository shoppingListRepository, final TelegramMessageCommandSender telegramMessageCommandSender) {
        this.shoppingListRepository = shoppingListRepository;
        this.telegramMessageCommandSender = telegramMessageCommandSender;
    }

    @Override
    public Policy getRequiredPolicy() {
        return Policy.SHOPPING_LIST_DELETER;
    }

    @Override
    public List<String> getCommand() {
        return List.of("market delete", "market remove");
    }

    @Override
    public void execute(Update update) {
        final String messageSanitized = update.getMessage().getText().toLowerCase().replace("market delete ", "").replace("market remove ", "").strip();
        final DeleteItemResponse deleteItemResponse = shoppingListRepository.delete(messageSanitized, update.getMessage().getChatId().toString());
        Log.infof("deleteItemResponse: %s", deleteItemResponse);

        final String message;
        if (deleteItemResponse.attributes().isEmpty()) {
            message = messageSanitized + " not found!";
        } else {
            message = messageSanitized + " deleted!";
        }

        telegramMessageCommandSender.executeSend(update.getMessage().getMessageId(), update.getMessage().getChatId().toString(), message);
    }
}

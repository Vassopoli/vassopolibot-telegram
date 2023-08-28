package com.alivassopoli.service;

import com.alivassopoli.adapter.dynamodb.ShoppingListItem;
import com.alivassopoli.adapter.dynamodb.ShoppingListRepository;
import com.alivassopoli.adapter.telegram.TelegramMessageCommandSender;
import com.alivassopoli.security.Policy;
import org.telegram.telegrambots.meta.api.objects.Update;

import javax.enterprise.context.ApplicationScoped;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

@ApplicationScoped
public class ShoppingListCreator implements VassopoliService {

    private final ShoppingListRepository shoppingListRepository;
    private final TelegramMessageCommandSender telegramMessageCommandSender;

    public ShoppingListCreator(final ShoppingListRepository shoppingListRepository, final TelegramMessageCommandSender telegramMessageCommandSender) {
        this.shoppingListRepository = shoppingListRepository;
        this.telegramMessageCommandSender = telegramMessageCommandSender;
    }

    @Override
    public Policy getRequiredPolicy() {
        return Policy.SHOPPING_LIST_CREATOR;
    }

    @Override
    public List<String> getCommand() {
        return List.of("market add");
    } //TODO: Estruturar de tal forma que fique tudo debaixo de um dominio, como de market neste caso

    @Override
    public void execute(final Update update) {
        final String messageSanitized = update.getMessage().getText().toLowerCase().replace("market add ", "");
        final Stream<String> streamOfItems = Arrays.stream(messageSanitized.split(","));

        streamOfItems
                .map(String::strip)
                .forEach(item -> {
                    shoppingListRepository.add(new ShoppingListItem(item, "market", update.getMessage().getChatId().toString(), LocalDate.now().toString()));

                    telegramMessageCommandSender.executeSend(update.getMessage().getMessageId(), update.getMessage().getChatId().toString(),
                            "Added " + item + "!");
        });
    }
}

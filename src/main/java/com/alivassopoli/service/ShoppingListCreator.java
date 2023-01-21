package com.alivassopoli.service;

import com.alivassopoli.adapter.dynamodb.ShoppingListItem;
import com.alivassopoli.adapter.dynamodb.ShoppingListRepository;
import com.alivassopoli.adapter.telegram.TelegramMessageSender;
import com.alivassopoli.security.Role;
import org.telegram.telegrambots.meta.api.objects.Update;

import javax.enterprise.context.ApplicationScoped;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

@ApplicationScoped
public class ShoppingListCreator implements VassopoliService {

    private final ShoppingListRepository shoppingListRepository;
    private final TelegramMessageSender telegramMessageSender;

    public ShoppingListCreator(final ShoppingListRepository shoppingListRepository, final TelegramMessageSender telegramMessageSender) {
        this.shoppingListRepository = shoppingListRepository;
        this.telegramMessageSender = telegramMessageSender;
    }

    @Override
    public Role getRequiredRole() {
        return Role.USER;
    }

    @Override
    public List<String> getCommand() {
        return List.of("market add");
    } //TODO: Estruturar de tal forma que fique tudo debaixo de um dominio, como de market neste caso

    @Override
    public void execute(Update update) {
        final String messageSanitized = update.getMessage().getText().toLowerCase().replace("market add ", "");
        final Stream<String> streamOfItems = Arrays.stream(messageSanitized.split(","));

        streamOfItems
                .map(String::strip)
                .forEach(item -> {
                    shoppingListRepository.add(new ShoppingListItem(item, "market", LocalDate.now().toString()));

                    telegramMessageSender.execute(update.getMessage().getMessageId(), update.getMessage().getChatId().toString(),
                            "Added " + item + "!");
        });
    }
}

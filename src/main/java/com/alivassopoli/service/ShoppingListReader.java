package com.alivassopoli.service;

import com.alivassopoli.adapter.dynamodb.ShoppingListItem;
import com.alivassopoli.adapter.dynamodb.ShoppingListRepository;
import com.alivassopoli.adapter.telegram.TelegramMessageCommandSender;
import com.alivassopoli.security.Policy;
import org.telegram.telegrambots.meta.api.objects.Update;
import software.amazon.awssdk.utils.StringUtils;

import javax.enterprise.context.ApplicationScoped;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class ShoppingListReader implements VassopoliService {

    private final ShoppingListRepository shoppingListRepository;
    private final TelegramMessageCommandSender telegramMessageCommandSender;

    public ShoppingListReader(final ShoppingListRepository shoppingListRepository, final TelegramMessageCommandSender telegramMessageCommandSender) {
        this.shoppingListRepository = shoppingListRepository;
        this.telegramMessageCommandSender = telegramMessageCommandSender;
    }

    @Override
    public Policy getRequiredPolicy() {
        return Policy.SHOPPING_LIST_READER;
    }

    @Override
    public List<String> getCommand() {
        return List.of("market get", "/marketget");
    }

    @Override
    public void execute(final Update update) {
        final List<ShoppingListItem> shoppingList = shoppingListRepository.findAll();

        final Comparator<ShoppingListItem> compareByCreatedAt = Comparator.comparing(x -> LocalDate.parse(x.getCreatedAt()));

        final Comparator<ShoppingListItem> compareByItem = Comparator.comparing(ShoppingListItem::getItem);

        final Comparator<ShoppingListItem> compare = compareByCreatedAt.reversed().thenComparing(compareByItem);

        final String finalMessage = "*Market List*\n\n" + shoppingList.stream()
                .sorted(compare)
                .map(this::getItemNameAndPastDays)
                .collect(Collectors.joining("\n"));

        telegramMessageCommandSender.executeSend(update.getMessage().getMessageId(), update.getMessage().getChatId().toString(),
                finalMessage, true);
    }

    private String getItemNameAndPastDays(final ShoppingListItem shoppingListItem) {
        final LocalDate today = LocalDate.now();
        final long diffInDays = Duration.between(LocalDate.parse(shoppingListItem.getCreatedAt()).atStartOfDay(), today.atStartOfDay()).toDays();

        return "\uD83D\uDED2 *" + StringUtils.capitalize(escapeEspecialCharacters(shoppingListItem.getItem())) + "*\n⏳ " + daysAgo(diffInDays) + "\n\\- \\- \\- \\- \\- \\- \\- \\- \\- \\- \\- \\- \\- \\- \\- \\- \\- \\- \\- \\-";
    }

    private String daysAgo(final long diffInDays) {
        if (diffInDays == 0) {
            return "today";
        } else if (diffInDays == 1) {
            return "yesterday";
        } else {
            return diffInDays + " days ago";
        }
    }

    //TODO: move to another place
    //TODO: make code dynamic
    private String escapeEspecialCharacters(final String message) {
        return message
                .replace("_", "\\_")
                .replace("*", "\\*")
                .replace("[", "\\[")
                .replace("]", "\\]")
                .replace("(", "\\(")
                .replace(")", "\\)")
                .replace("~", "\\~")
                .replace("`", "\\`")
                .replace(">", "\\>")
                .replace("#", "\\#")
                .replace("+", "\\+")
                .replace("-", "\\-")
                .replace("=", "\\=")
                .replace("|", "\\|")
                .replace("{", "\\{")
                .replace("}", "\\}")
                .replace(".", "\\.")
                .replace("!", "\\!");
    }
}

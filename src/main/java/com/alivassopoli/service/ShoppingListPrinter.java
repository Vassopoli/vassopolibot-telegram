package com.alivassopoli.service;

import com.alivassopoli.adapter.dynamodb.ShoppingListItem;
import com.alivassopoli.adapter.dynamodb.ShoppingListRepository;
import com.alivassopoli.adapter.telegram.TelegramMessageCommandSender;
import com.alivassopoli.adapter.twilio.EmailSender;
import com.alivassopoli.security.Role;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.telegram.telegrambots.meta.api.objects.Update;

import javax.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@ApplicationScoped
public class ShoppingListPrinter implements VassopoliService {
    private static final Logger LOG = Logger.getLogger(ShoppingListPrinter.class);

    private final String printerEmail;
    private final ShoppingListRepository shoppingListRepository;
    private final EmailSender emailSender;
    private final TelegramMessageCommandSender telegramMessageCommandSender;

    public ShoppingListPrinter(@ConfigProperty(name = "vassopolibot-telegram-webhook.printer.email") final String printerEmail,
                               final ShoppingListRepository shoppingListRepository,
                               final EmailSender emailSender,
                               final TelegramMessageCommandSender telegramMessageCommandSender) {
        this.printerEmail = printerEmail;
        this.shoppingListRepository = shoppingListRepository;
        this.emailSender = emailSender;
        this.telegramMessageCommandSender = telegramMessageCommandSender;
    }

    @Override
    public Role getRequiredRole() {
        return Role.USER;
    }

    @Override
    public List<String> getCommand() {
        return List.of("market print");
    }

    @Override
    public void execute(final Update update) {
        LOG.info("ShoppingListPrinter - execute");
        final List<ShoppingListItem> shoppingList = shoppingListRepository.findAll();

        final String html = getTemplateFormatted(shoppingList);

        LOG.info("ShoppingListPrinter - sending email...");
        emailSender.execute(printerEmail, html); //TODO: email could be async

        telegramMessageCommandSender.executeSend(update.getMessage().getMessageId(), update.getMessage().getChatId().toString(), "Sent to printer!");
    }

    private String getElementFormatted(final ShoppingListItem element) {
        return "<li style=\"margin-bottom: 0.6rem;\">" + element.getItem() + "</li>";
    }

    private String getTemplateFormatted(final List<ShoppingListItem> shoppingList) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("index.html")) {
            Objects.requireNonNull(is);

            final String shoppingListResult = shoppingList.stream()
                    .map(this::getElementFormatted)
                    .collect(Collectors.joining());

            return new String(is.readAllBytes(), StandardCharsets.UTF_8)
                    .replace("[[ITEM_SIZE]]", String.valueOf(shoppingList.size()))
                    .replace("[[ITEM]]", shoppingListResult);
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new RuntimeException();
        }
    }
}

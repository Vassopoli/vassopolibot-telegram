package com.alivassopoli.adapter.resources;

import com.alivassopoli.adapter.telegram.VassopoliBotTelegramWebhook;
import com.alivassopoli.configuration.MyURLs;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import org.telegram.telegrambots.updatesreceivers.ServerlessWebhook;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/telegram-bot")
public class TelegramBotResource {
    private static final Logger LOG = Logger.getLogger(TelegramBotResource.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final String url;
    private final VassopoliBotTelegramWebhook vassopoliBotTelegramWebHook;

    public TelegramBotResource(@ConfigProperty(name = "vassopolibot-telegram-webhook.base-url") final String baseUrl,
                               final VassopoliBotTelegramWebhook vassopoliBotTelegramWebHook) {
        this.url = baseUrl + "/telegram-bot";
        this.vassopoliBotTelegramWebHook = vassopoliBotTelegramWebHook;
    }

    @GET
    @Path(MyURLs.TELEGRAM_REGISTER_URL)
    public String registerWebhook() {
        final String URL_LOG = "URL: " + url;

        try {
            final TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class, new ServerlessWebhook());
            telegramBotsApi.registerBot(vassopoliBotTelegramWebHook, SetWebhook.builder().url(url).build());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        LOG.info(URL_LOG);
        return URL_LOG;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path(MyURLs.TELEGRAM_CALLBACK_URL)
//    public BotApiMethod<?> onUpdateReceived(final Update update) {
    public BotApiMethod<?> onUpdateReceived(final String updateJsonString) throws JsonProcessingException {
        final Update update = objectMapper.readValue(updateJsonString, Update.class);
        return vassopoliBotTelegramWebHook.onWebhookUpdateReceived(update);
    }
}

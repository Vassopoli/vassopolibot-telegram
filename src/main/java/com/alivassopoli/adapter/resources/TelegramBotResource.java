package com.alivassopoli.adapter.resources;

import com.alivassopoli.adapter.telegram.VassopoliBotTelegramWebhook;
import com.alivassopoli.configuration.MyURLs;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.commands.GetMyCommands;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import org.telegram.telegrambots.updatesreceivers.ServerlessWebhook;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

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
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path(MyURLs.TELEGRAM_REGISTER_WEBHOOK_URL)
    public Response registerWebhook() throws JsonProcessingException {
        final Map<String, String> webhookUrl = Map.of("webhookUrl", url);

        try {
            final TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class, new ServerlessWebhook());
            telegramBotsApi.registerBot(vassopoliBotTelegramWebHook, SetWebhook.builder().url(url).build());
            LOG.info(webhookUrl);

            return Response
                    .status(Response.Status.OK)
                    .entity(objectMapper.writeValueAsString(webhookUrl))
                    .build();
        } catch (TelegramApiException ex) {
            LOG.error("Error registering bot commands", ex);
            return Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(objectMapper.writeValueAsString(Map.of()))
                    .build();
        }
    }

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path(MyURLs.TELEGRAM_REGISTER_COMMANDS_URL)
    public Response registerCommands() throws JsonProcessingException {
        final GetMyCommands getMyCommands = GetMyCommands.builder().build();

        //TODO: Populate automatically using Instance list
        final BotCommand botCommand1 = BotCommand.builder().command("/start").description("Start interaction with vassopoli").build();
        final BotCommand botCommand2 = BotCommand.builder().command("/marketget").description("Get items from market list").build();
        final SetMyCommands setMyCommands = SetMyCommands.builder().commands(List.of(botCommand1, botCommand2)).build();

        try {
            final List<BotCommand> previousBotCommandList = vassopoliBotTelegramWebHook.execute(getMyCommands);
            LOG.infof("Previous command list was %s", previousBotCommandList);

            vassopoliBotTelegramWebHook.execute(setMyCommands);
            LOG.infof("New command list is %s", setMyCommands.getCommands());

            return Response
                    .status(Response.Status.OK)
                    .entity(objectMapper.writeValueAsString(setMyCommands.getCommands()))
                    .build();
        } catch (TelegramApiException ex) {
            LOG.error("Error registering bot commands", ex);
            return Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(objectMapper.writeValueAsString(List.of()))
                    .build();
        }
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

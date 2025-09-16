package com.alivassopoli.service;

import com.alivassopoli.adapter.telegram.TelegramMessageCommandSender;
import com.alivassopoli.security.Policy;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.telegrambots.meta.api.objects.Update;

import javax.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

@ApplicationScoped
public class LlmService implements VassopoliService {

    private static final Logger LOG = Logger.getLogger(LlmService.class);
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=";

    private final TelegramMessageCommandSender telegramMessageCommandSender;

    @ConfigProperty(name = "gemini.api.key")
    String apiKey;

    public LlmService(final TelegramMessageCommandSender telegramMessageCommandSender) {
        this.telegramMessageCommandSender = telegramMessageCommandSender;
    }

    @Override
    public Policy getRequiredPolicy() {
        return Policy.LLM_USER;
    }

    @Override
    public List<String> getCommand() {
        return List.of("llm");
    }

    @Override
    public void execute(final Update update) {
        final String messageSanitized = update.getMessage().getText().toLowerCase().replace("llm ", "");

        try {
            HttpClient client = HttpClient.newHttpClient();

            JSONObject textPart = new JSONObject();
            textPart.put("text", messageSanitized);

            JSONObject content = new JSONObject();
            content.put("parts", new JSONArray().put(textPart));

            JSONObject body = new JSONObject();
            body.put("contents", new JSONArray().put(content));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GEMINI_API_URL + apiKey))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            LOG.infof("Gemini response status code: %d", response.statusCode());
            LOG.info("Gemini response: " + response.body());

            if (response.statusCode() != 200) {
                telegramMessageCommandSender.executeSend(update.getMessage().getMessageId(), update.getMessage().getChatId().toString(), "Error from Gemini API. Status code: " + response.statusCode());
                return;
            }

            try {
                JSONObject responseBody = new JSONObject(response.body());
                if (responseBody.has("candidates")) {
                    String textResponse = responseBody.getJSONArray("candidates").getJSONObject(0).getJSONObject("content").getJSONArray("parts").getJSONObject(0).getString("text");
                    telegramMessageCommandSender.executeSend(update.getMessage().getMessageId(), update.getMessage().getChatId().toString(), textResponse);
                } else {
                    LOG.error("Gemini response does not contain 'candidates' field.");
                    telegramMessageCommandSender.executeSend(update.getMessage().getMessageId(), update.getMessage().getChatId().toString(), "Error: Could not parse Gemini response.");
                }
            } catch (JSONException e) {
                LOG.error("Error parsing Gemini response JSON", e);
                telegramMessageCommandSender.executeSend(update.getMessage().getMessageId(), update.getMessage().getChatId().toString(), "Error: Could not parse Gemini response.");
            }

        } catch (IOException | InterruptedException e) {
            LOG.error("Error sending request to Gemini API", e);
            throw new RuntimeException(e);
        }
    }
}

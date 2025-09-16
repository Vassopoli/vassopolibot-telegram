package com.alivassopoli.service;

import com.alivassopoli.adapter.telegram.TelegramMessageCommandSender;
import com.alivassopoli.security.Policy;
import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.telegram.telegrambots.meta.api.objects.Update;

import javax.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.util.List;

@ApplicationScoped
public class LlmService implements VassopoliService {

    private final TelegramMessageCommandSender telegramMessageCommandSender;

    @ConfigProperty(name = "gemini.project.id")
    String projectId;

    @ConfigProperty(name = "gemini.location")
    String location;

    @ConfigProperty(name = "gemini.model.name")
    String modelName;


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

        try (VertexAI vertexAI = new VertexAI(projectId, location)) {
            GenerativeModel model = new GenerativeModel(modelName, vertexAI);

            final GenerateContentResponse generateContentResponse = model.generateContent(messageSanitized);
            final String response = ResponseHandler.getText(generateContentResponse);

            telegramMessageCommandSender.executeSend(update.getMessage().getMessageId(), update.getMessage().getChatId().toString(), response);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

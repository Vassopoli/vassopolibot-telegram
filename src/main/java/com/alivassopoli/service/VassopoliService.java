package com.alivassopoli.service;

import com.alivassopoli.security.Policy;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

public interface VassopoliService {

    Policy getRequiredPolicy();

    List<String> getCommand();

    void execute(Update update);
}

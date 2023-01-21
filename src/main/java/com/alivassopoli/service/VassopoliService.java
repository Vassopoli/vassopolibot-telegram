package com.alivassopoli.service;

import com.alivassopoli.security.Role;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

public interface VassopoliService {

    Role getRequiredRole();

    List<String> getCommand();

    void execute(Update update);
}

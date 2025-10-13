package com.alivassopoli.adapter.resources.dto;

import com.alivassopoli.security.Role;
import org.telegram.telegrambots.meta.api.objects.Update;

public class ExecuteRequest {
    public Update update;
    public Role role;
}

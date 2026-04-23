package dev.m2g2.simao.dto.chat;

import dev.m2g2.simao.dto.TaskDTO;

public record TaskChatResponse(String text, boolean completed, TaskDTO task)
    implements ChatResponse {}

package dev.m2g2.simao.service;

import dev.m2g2.simao.model.chat.ChatRecord;

public interface InteractionBaseService {
    String createInteractionIf(String incomingMessage);
    String createIf(Object pairCandidate, ChatRecord record);
    String listIf(String incomingMessage);
    String deleteIf(String incomingMessage);
}

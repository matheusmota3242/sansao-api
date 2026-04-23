package dev.m2g2.simao.service;

public interface InteractionBaseService {
    String createInteractionIf(String incomingMessage);
    String listIf(String incomingMessage);
    String deleteIf(String incomingMessage);
}

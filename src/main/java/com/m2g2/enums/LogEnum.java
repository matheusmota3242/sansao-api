package com.m2g2.enums;

public enum LogEnum {

    ENTRADA("Método: {} - Request payload: {}"),
    SAIDA("Método: {} - Response payload: {}");

    private final String mensagem;

    LogEnum(String mensagem) {
        this.mensagem = mensagem;
    }

    public String getMensagem() {
        return mensagem;
    }
}

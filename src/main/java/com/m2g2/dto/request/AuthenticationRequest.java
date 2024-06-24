package com.m2g2.dto.request;

public record AuthenticationRequest(String firstname,
                                    String lastname,
                                    String username,
                                    String password) {
}

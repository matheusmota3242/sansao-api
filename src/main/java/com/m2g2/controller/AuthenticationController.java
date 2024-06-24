package com.m2g2.controller;

import com.m2g2.dto.request.AuthenticationRequest;
import com.m2g2.dto.response.AuthenticationResponse;
import com.m2g2.service.AuthenticationService;
import com.m2g2.service.UserDetailsServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/auth")
@CrossOrigin
public class AuthenticationController {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationController.class);
    public static final String VALIDATE_TOKEN = "validateToken";

    private final AuthenticationService service;

    public AuthenticationController(AuthenticationService service) {
        this.service = service;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@RequestBody AuthenticationRequest request) {
        LOGGER.info("Method: {} - Request payload: {}", "register", request);
        AuthenticationResponse response = service.register(request);
        LOGGER.info("Method: {} - Response payload: {}", "register", response);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        LOGGER.info("Method: {} - Request payload: {}", "authenticate", request);
        AuthenticationResponse response = service.authenticate(request);
        LOGGER.info("Method: {} - Response payload: {}", "authenticate", response);
        return ResponseEntity.ok(response);
    }

    @GetMapping("validate")
    public void validateToken() {
        LOGGER.info("Method: {}", VALIDATE_TOKEN);
    }
}

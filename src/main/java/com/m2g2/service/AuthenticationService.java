package com.m2g2.service;

import com.m2g2.config.JwtService;
import com.m2g2.dto.request.AuthenticationRequest;
import com.m2g2.dto.response.AuthenticationResponse;
import com.m2g2.model.Role;
import com.m2g2.model.User;
import com.m2g2.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    private final AuthenticationManager authenticationManager;

    public AuthenticationService(UserRepository repository, PasswordEncoder passwordEncoder, JwtService jwtService, AuthenticationManager authenticationManager) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public AuthenticationResponse register(AuthenticationRequest request) {
        User user = new User();
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setFirstname(request.firstname());
        user.setLastname(request.lastname());
        user.setRole(Role.USER);
        repository.save(user);
        String jwt = jwtService.generateToken(user);
        return new AuthenticationResponse(jwt);
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                request.username(),
                request.password()
        ));
        User user = repository.findByUsername(request.username()).orElseThrow(() -> new UsernameNotFoundException("User not found."));
        String jwt = jwtService.generateToken(user);
        return new AuthenticationResponse(jwt);
    }
}

package com.m2g2.service;

import com.m2g2.config.JwtService;
import com.m2g2.dto.response.WorkoutDTO;
import com.m2g2.model.Workout;
import com.m2g2.repository.WorkoutRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WorkoutService {

    private final WorkoutRepository repository;

    private final JwtService jwtService;

    private final HttpServletRequest request;

    public WorkoutService(WorkoutRepository repository, JwtService jwtService, HttpServletRequest request) {
        this.repository = repository;
        this.jwtService = jwtService;
        this.request = request;
    }


    public void save(Workout workout) {
        String token = request.getHeader(HttpHeaders.AUTHORIZATION);
        String username = jwtService.extractUsername(token);
        workout.setUsername(username);
        repository.save(workout);
    }

    public List<WorkoutDTO> getAll() {
        String token = request.getHeader(HttpHeaders.AUTHORIZATION);
        String username = jwtService.extractUsername(token);
        return repository.findAll(username);
    }

    public Workout get(String id) {
        return repository.find(id);
    }

    public void update(Workout workout) {
        if (workout.getId() != null && !workout.getId().isBlank()) {
            repository.update(workout);
        } else {
            throw new IllegalArgumentException("The 'id' field must be filled.");
        }
    }

    public void delete(String id) {
        repository.delete(id);
    }
}

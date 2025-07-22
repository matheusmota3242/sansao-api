package com.m2g2.controller;

import com.m2g2.dto.response.WorkoutDTO;
import com.m2g2.model.Workout;
import com.m2g2.service.WorkoutService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/workouts")
@CrossOrigin
public class WorkoutController {

    public static final String GET_ALL_WORKOUTS = "getAllWorkouts";
    private final WorkoutService service;

    public WorkoutController(WorkoutService service) {
        this.service = service;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkoutController.class);

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void save(@Valid @RequestBody Workout workout) {
        service.save(workout);
    }

    @GetMapping("{id}")
    public Workout get(@PathVariable("id") String id) {
        LOGGER.info("Method: {} | Request: {}", "get", id);
        Workout workout = service.get(id);
        LOGGER.info("Method: {} | Response: {}", "get", workout);
        return workout;
    }
    @GetMapping
    public List<WorkoutDTO> getAllWorkouts() {
        LOGGER.info("Method: {}", GET_ALL_WORKOUTS);
        List<WorkoutDTO> workouts = service.getAll();
        LOGGER.info("Method: {} | Response: {}", GET_ALL_WORKOUTS, workouts);
        return workouts;
    }

    @PutMapping
    public void update(@RequestBody Workout workout) {
        service.update(workout);
    }

    @DeleteMapping("{id}")
    public void delete(@PathVariable("id") String id) {
        service.delete(id);
    }
}

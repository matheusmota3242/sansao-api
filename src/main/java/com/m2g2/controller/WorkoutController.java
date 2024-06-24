package com.m2g2.controller;

import com.m2g2.dto.response.WorkoutDTO;
import com.m2g2.model.Workout;
import com.m2g2.service.WorkoutService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/workouts")
public class WorkoutController {

    private final WorkoutService service;

    public WorkoutController(WorkoutService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void save(@Valid @RequestBody Workout workout) {
        service.save(workout);
    }

    @GetMapping("{id}")
    public Workout get(@PathVariable("id") String id) {
        return service.get(id);
    }
    @GetMapping
    public List<WorkoutDTO> getAllWorkouts() {
        return service.getAll();
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

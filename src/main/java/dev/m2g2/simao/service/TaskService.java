package dev.m2g2.simao.service;

import dev.m2g2.simao.model.Task;
import dev.m2g2.simao.repository.TaskRepository;
import org.springframework.stereotype.Service;

@Service
public class TaskService {
    private final TaskRepository repository;

    public TaskService(TaskRepository repository) {
        this.repository = repository;
    }

    private Task getById(Long id) {
        return repository.findById(id).orElse(null);
    }

    public Task create(Task task) {
        return repository.save(task);
    }
}

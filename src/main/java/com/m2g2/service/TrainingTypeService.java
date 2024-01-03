package com.m2g2.service;

import com.m2g2.dto.response.TrainingTypeResponse;
import com.m2g2.model.TrainingType;
import com.m2g2.repository.TrainingTypeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TrainingTypeService {

    private final TrainingTypeRepository repository;

    public TrainingTypeService(TrainingTypeRepository repository) {
        this.repository = repository;
    }

    public List<TrainingType> getAll() {
        return repository.findAll();
    }

    public List<TrainingTypeResponse> getAllNames() {
        //return repository.findAll().stream().map(trainingType -> new TrainingTypeResponse(trainingType.getId(), trainingType.getName())).toList();
        return repository.findAllTrainingTypeName();
    }

    public Optional<TrainingType> getByName(String name) {
        return repository.findByName(name);
    }

    public void save(TrainingType trainingType) {
        repository.save(trainingType);
    }
}

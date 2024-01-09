package com.m2g2.service;

import com.m2g2.dto.response.TrainingReferenceResponse;
import com.m2g2.dto.response.TrainingResponse;
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
        return repository.findAllTrainingTypeName();
    }

    public List<TrainingReferenceResponse> getAllTrainingReferenceResponses() {
        return repository.findAllTrainingReferences();
    }

    public Optional<TrainingType> getByName(String name) {
        return repository.findByName(name);
    }

    public Optional<TrainingResponse> getTrainingResponseById(Long id) {
        return repository.findTrainingResponseById(id);
    }

    public void save(TrainingType trainingType) {
        repository.save(trainingType);
    }
}

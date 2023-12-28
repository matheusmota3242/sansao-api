package com.m2g2.service;

import com.m2g2.dto.request.TrainingRequest;
import com.m2g2.model.Load;
import com.m2g2.model.Training;
import com.m2g2.model.TrainingItem;
import com.m2g2.model.TrainingType;
import com.m2g2.repository.LoadRepository;
import com.m2g2.repository.TrainingItemRepository;
import com.m2g2.repository.TrainingRepository;
import com.m2g2.repository.TrainingTypeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TrainingService {

	private final TrainingRepository repository;

	private final TrainingTypeRepository trainingTypeRepository;

	private final TrainingItemRepository trainingItemRepository;

	private final LoadRepository loadRepository;

	public TrainingService(TrainingRepository repository, TrainingTypeRepository trainingTypeRepository, TrainingItemRepository trainingItemRepository, LoadRepository loadRepository) {
		this.repository = repository;
		this.trainingTypeRepository = trainingTypeRepository;
		this.trainingItemRepository = trainingItemRepository;
		this.loadRepository = loadRepository;
	}

	public void save(TrainingRequest request) {
		Optional<TrainingType> optional = trainingTypeRepository.findByName(request.name());
		TrainingType trainingType = optional.orElse(new TrainingType(request.name()));
		Training training = request.training();

		if (training.getId() == null) {
			trainingType.getTrainings().add(training);
		} else {
			trainingType.getTrainings().replaceAll(t -> t.getId().equals(training.getId()) ? training : t);
		}

		trainingTypeRepository.save(trainingType);
	}

	public void update(TrainingRequest request) {

	}

	public List<Training> getAll() {
		return repository.findAll();
	}
}

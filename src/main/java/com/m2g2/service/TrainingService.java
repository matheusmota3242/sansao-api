package com.m2g2.service;

import com.m2g2.dto.request.TrainingRequest;
import com.m2g2.dto.response.TrainingResponse;
import com.m2g2.dto.response.TrainingTypeResponse;
import com.m2g2.model.Load;
import com.m2g2.model.Training;
import com.m2g2.model.TrainingItem;
import com.m2g2.model.TrainingType;
import com.m2g2.repository.LoadRepository;
import com.m2g2.repository.TrainingItemRepository;
import com.m2g2.repository.TrainingRepository;
import com.m2g2.repository.TrainingTypeRepository;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TrainingService {

	private final TrainingRepository repository;

	private final TrainingTypeService trainingTypeService;

	public TrainingService(TrainingRepository repository, TrainingTypeService trainingTypeService) {
		this.repository = repository;
		this.trainingTypeService = trainingTypeService;

	}

	public void save(TrainingRequest request) {
		Optional<TrainingType> optional = trainingTypeService.getByName(request.name());
		TrainingType trainingType = optional.orElse(new TrainingType(request.name()));
		Training training = request.training();

		if (training.getId() == null) {
			trainingType.getTrainings().add(training);
		} else {
			trainingType.getTrainings().replaceAll(t -> t.getId().equals(training.getId()) ? training : t);
		}

		trainingTypeService.save(trainingType);
	}

	public void update(TrainingRequest request) {

	}

	public List<TrainingResponse> getAll() {
		List<TrainingType> trainingTypes = trainingTypeService.getAll();
		List<TrainingResponse> trainingResponses = trainingTypes
				.stream()
				.flatMap(trainingType -> trainingType.getTrainings()
						.stream()
						.map(training -> new TrainingResponse(trainingType.getName(), training)))
				.collect(Collectors.toList());
		Collections.sort(trainingResponses, Comparator.comparing((trainingResponse) -> trainingResponse.training().getStartTime()));
		return trainingResponses;
	}
}
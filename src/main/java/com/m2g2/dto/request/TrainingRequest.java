package com.m2g2.dto.request;

import com.m2g2.model.Training;
import com.m2g2.model.TrainingItem;
import com.m2g2.model.TrainingType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public record TrainingRequest(@NotBlank(message = "Campo 'name' não pode ser vazio ou nulo") String name,
                              Training training) {}

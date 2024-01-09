package com.m2g2.dto.response;

import java.time.LocalDateTime;

public record TrainingReferenceResponse(Long id, String trainingTypeName, LocalDateTime startTime) {
}

package com.m2g2.dto.response;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record WorkoutDTO(String id,
                         String description,
                         String start,
                         String end) {}
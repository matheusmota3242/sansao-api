package com.m2g2.repository;

import com.m2g2.dto.response.TrainingTypeResponse;
import com.m2g2.model.TrainingType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrainingTypeRepository extends JpaRepository<TrainingType, Long> {
    Optional<TrainingType> findByName(String name);

    @Query("SELECT NEW com.m2g2.dto.response.TrainingTypeResponse(tt.id, tt.name) FROM TrainingType tt")
    List<TrainingTypeResponse> findAllTrainingTypeName();


}

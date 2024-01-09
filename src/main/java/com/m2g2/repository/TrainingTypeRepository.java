package com.m2g2.repository;

import com.m2g2.dto.response.TrainingReferenceResponse;
import com.m2g2.dto.response.TrainingResponse;
import com.m2g2.dto.response.TrainingTypeResponse;
import com.m2g2.model.TrainingType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrainingTypeRepository extends JpaRepository<TrainingType, Long> {
    Optional<TrainingType> findByName(String name);

    @Query("SELECT NEW com.m2g2.dto.response.TrainingTypeResponse(tt.id, tt.name) FROM TrainingType tt")
    List<TrainingTypeResponse> findAllTrainingTypeName();

    @Query("SELECT NEW com.m2g2.dto.response.TrainingReferenceResponse(tt.id, tt.name, t.startTime) FROM TrainingType tt JOIN tt.trainings t")
    List<TrainingReferenceResponse> findAllTrainingReferences();

    @Query("SELECT NEW com.m2g2.dto.response.TrainingResponse(tt.name, t) FROM TrainingType tt JOIN tt.trainings t  WHERE t.id = :id")

    Optional<TrainingResponse> findTrainingResponseById(@Param("id") Long id);
}

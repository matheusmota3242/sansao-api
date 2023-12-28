package com.m2g2.repository;

import com.m2g2.model.TrainingItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrainingItemRepository extends JpaRepository<TrainingItem, Long> {}

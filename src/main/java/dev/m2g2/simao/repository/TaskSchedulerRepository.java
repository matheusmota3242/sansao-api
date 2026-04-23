package dev.m2g2.simao.repository;

import dev.m2g2.simao.model.task.TaskScheduler;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskSchedulerRepository extends JpaRepository<TaskScheduler, Long> {}

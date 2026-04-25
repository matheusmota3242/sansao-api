package dev.m2g2.simao.cron;

import dev.m2g2.simao.service.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class TaskJobService {
    private final Logger log = LoggerFactory.getLogger(TaskJobService.class);

    private final TaskService taskService;

    public TaskJobService(TaskService taskService) {
        this.taskService = taskService;
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void recreateDailyTasks() {
        
    }

}

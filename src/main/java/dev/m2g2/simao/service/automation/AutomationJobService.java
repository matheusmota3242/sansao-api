package dev.m2g2.simao.service.automation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class AutomationJobService {

    private final AutomationService automationService;

    public AutomationJobService(AutomationService automationService) {
        this.automationService = automationService;
    }

    private final Logger log = LoggerFactory.getLogger(AutomationJobService.class);

    /**
     * Runs every day for every 5 minutes from 00:00
     */
    @Scheduled(cron = "0 */5 * * * *")
    public void executeBatch() {
        log.info("Executing batch");
        automationService.executeBatch();
    }
}

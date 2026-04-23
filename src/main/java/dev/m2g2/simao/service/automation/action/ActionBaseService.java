package dev.m2g2.simao.service.automation.action;

import dev.m2g2.simao.dto.automation.ExecutionResult;
import dev.m2g2.simao.model.automation.Automation;

public interface ActionBaseService {
    ExecutionResult execute(Automation automation);
}

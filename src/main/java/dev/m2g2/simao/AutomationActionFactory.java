package dev.m2g2.simao;

import dev.m2g2.simao.annotation.AutomationAction;
import dev.m2g2.simao.enums.ActionType;
import dev.m2g2.simao.service.automation.action.ActionBaseService;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class AutomationActionFactory {

    private final Map<ActionType, ActionBaseService> services = new EnumMap<>(ActionType.class);

    public AutomationActionFactory(List<ActionBaseService> actionServices) {
        for (ActionBaseService actionService : actionServices) {
            Class<?> targetClass = AopUtils.getTargetClass(actionService);
            AutomationAction annotation = AnnotationUtils.getAnnotation(targetClass, AutomationAction.class);
            if (annotation != null) {
                services.put(annotation.actionType(), actionService);
            }
        }
    }

    public ActionBaseService getActionServiceByType(ActionType type) {
        return services.get(type);
    }
}

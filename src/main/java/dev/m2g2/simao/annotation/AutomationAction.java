package dev.m2g2.simao.annotation;

import dev.m2g2.simao.enums.ActionType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE}) // Pode ser usada em classes ou métodos
@Retention(RetentionPolicy.RUNTIME)
public @interface AutomationAction {
    ActionType actionType();
}

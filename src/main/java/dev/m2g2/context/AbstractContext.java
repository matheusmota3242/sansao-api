package dev.m2g2.context;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public abstract class AbstractContext {
    protected Type type;
    protected Integer stepIndex;
    protected List<Step> steps = new ArrayList<>();
    public enum Type {
        NEW_TRANSACTION;
    }

    static public class Step {
        private String message;
        private Function<String, Optional<Object>> processInput;

        public Step(String message, Function<String, Optional<Object>> processInput) {
            this.message = message;
            this.processInput = processInput;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public Function<String, Optional<Object>> getProcessInput() {
            return processInput;
        }
    }

    public Type getType() {
        return type;
    }

    public Integer getStepIndex() {
        return stepIndex;
    }

    public void setStepIndex(Integer stepIndex) {
        this.stepIndex = stepIndex;
    }

    public List<Step> getSteps() {
        return steps;
    }

    public String getMessage() {
        return steps.get(stepIndex).message;
    }

    public Optional<Object> processInput(String value) {
        Optional<Object> optionalOutput = Optional.empty();
        if (steps.get(stepIndex).processInput != null) {
            optionalOutput = steps.get(stepIndex).processInput.apply(value);
        }
        return optionalOutput;
    }
}

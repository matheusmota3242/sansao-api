package dev.m2g2.simao.model.chat;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import dev.m2g2.simao.model.chat.task.CreateTaskInteraction;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = CreateTaskInteraction.class, name = "create_task")
})
public abstract class Interaction<T>  {

    protected List<Step> steps = new ArrayList<>();

    protected T data;

    public List<Step> getSteps() {
        return steps;
    }

    public void setSteps(List<Step> steps) {
        this.steps = steps;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @JsonIgnore
    public Step getCurrentStep() {
        return steps.stream().filter(step -> !step.isCompleted()).findFirst().orElse(null);
    }

    @JsonIgnore
    public Object processInput(String value) {
        Optional<Object> output = getCurrentStep().execute(value);
        return output.orElseGet(() -> getCurrentStep().getDescription());
    }

}

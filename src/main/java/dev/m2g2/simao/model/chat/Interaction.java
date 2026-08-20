package dev.m2g2.simao.model.chat;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import dev.m2g2.simao.dto.chat.ChatResponse;
import dev.m2g2.simao.model.chat.automation.CreateAutomationInteraction;
import dev.m2g2.simao.model.chat.note.CreateNoteInteraction;
import dev.m2g2.simao.model.chat.order.CreateOrderInteraction;
import dev.m2g2.simao.model.chat.order.UpdateOrderInteraction;
import dev.m2g2.simao.model.chat.purchase.CreatePurchaseInteraction;
import dev.m2g2.simao.model.chat.purchase.UpdatePurchaseInteraction;
import dev.m2g2.simao.model.chat.task.CreateTaskInteraction;
import dev.m2g2.simao.model.chat.tracker.CreateTrackerInteraction;

import java.util.ArrayList;
import java.util.List;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = CreateTaskInteraction.class),
        @JsonSubTypes.Type(value = CreateAutomationInteraction.class),
        @JsonSubTypes.Type(value = CreateNoteInteraction.class),
        @JsonSubTypes.Type(value = CreatePurchaseInteraction.class),
        @JsonSubTypes.Type(value = UpdatePurchaseInteraction.class),
        @JsonSubTypes.Type(value = CreateOrderInteraction.class),
        @JsonSubTypes.Type(value = UpdateOrderInteraction.class),
        @JsonSubTypes.Type(value = CreateTrackerInteraction.class)
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
    public abstract ChatResponse processInput(String value);

    public abstract String cancelMessage();
}

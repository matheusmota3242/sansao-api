package dev.m2g2.simao.model.chat.tracker;

import com.fasterxml.jackson.annotation.JsonTypeName;
import dev.m2g2.simao.dto.TrackerDTO;
import dev.m2g2.simao.dto.chat.TrackerChatResponse;
import dev.m2g2.simao.enums.ChatType;
import dev.m2g2.simao.model.chat.Interaction;
import dev.m2g2.simao.model.chat.Step;

import java.math.BigDecimal;
import java.util.List;
import java.util.regex.Pattern;

import static dev.m2g2.simao.dto.chat.TrackerChatResponse.error;
import static dev.m2g2.simao.dto.chat.TrackerChatResponse.proceed;
import static dev.m2g2.simao.dto.chat.TrackerChatResponse.success;

@JsonTypeName("create_tracker")
public class CreateTrackerInteraction extends Interaction<TrackerDTO> {

    // Keyword must start with a letter so it can never be parsed as an amount,
    // and stays within the routing "@<keyword> <amount>" grammar.
    private static final Pattern KEYWORD_PATTERN = Pattern.compile("^[a-z][a-z0-9]{1,19}$");

    public CreateTrackerInteraction() {
        super();
        this.steps.addAll(List.of(
                new Step("Nome do acompanhamento:\n_Ex: Água_"),
                new Step("""
                        Apelido para registrar (sem espaços, começando por letra):
                        _Ex: agua — depois você registra com @agua 250_
                        """),
                new Step("Unidade de medida:\n_Ex: ml_"),
                new Step("Meta diária (número):\n_Ex: 2000_")
        ));
        this.data = new TrackerDTO();
    }

    @Override
    public TrackerChatResponse processInput(String value) {
        if (value.equalsIgnoreCase(ChatType.CREATE_TRACKER.getValue()))
            return proceed(this.steps.getFirst().getDescription());

        if (this.steps.get(0).equals(getCurrentStep()))
            return executeNameStep(value);

        if (this.steps.get(1).equals(getCurrentStep()))
            return executeKeywordStep(value);

        if (this.steps.get(2).equals(getCurrentStep()))
            return executeUnitStep(value);

        return executeGoalStep(value);
    }

    @Override
    public String cancelMessage() {
        return "Criação de acompanhamento cancelada.";
    }

    private TrackerChatResponse executeNameStep(String value) {
        if (value == null || value.isBlank())
            return error("Nome não pode ser vazio. Tente novamente.");
        this.data.setName(value.trim());
        this.steps.get(0).setCompleted(true);
        return proceed(getCurrentStep().getDescription());
    }

    private TrackerChatResponse executeKeywordStep(String value) {
        if (value == null)
            return error("Apelido inválido. Tente novamente.");
        String keyword = value.trim().toLowerCase();
        if (!KEYWORD_PATTERN.matcher(keyword).matches())
            return error("Apelido inválido. Use só letras/números, sem espaço, começando por letra. _Ex: agua_");
        if (isReservedCommand(keyword))
            return error("Esse apelido conflita com um comando existente. Escolha outro.");
        this.data.setKeyword(keyword);
        this.steps.get(1).setCompleted(true);
        return proceed(getCurrentStep().getDescription());
    }

    private TrackerChatResponse executeUnitStep(String value) {
        if (value == null || value.isBlank())
            return error("Unidade não pode ser vazia. Tente novamente.");
        this.data.setUnit(value.trim());
        this.steps.get(2).setCompleted(true);
        return proceed(getCurrentStep().getDescription());
    }

    private TrackerChatResponse executeGoalStep(String value) {
        BigDecimal goal = parseAmount(value);
        if (goal == null || goal.signum() <= 0)
            return error("Meta inválida. Informe um número positivo. _Ex: 2000_");
        this.data.setDailyGoal(goal);
        return success("Acompanhamento cadastrado com sucesso!", this.data);
    }

    // "@" + keyword must not collide with any static command (@ltask, @menu, ...).
    private boolean isReservedCommand(String keyword) {
        String candidate = "@" + keyword;
        for (ChatType chatType : ChatType.values()) {
            if (chatType.getValue().equalsIgnoreCase(candidate))
                return true;
        }
        return false;
    }

    private BigDecimal parseAmount(String value) {
        if (value == null || value.isBlank())
            return null;
        try {
            return new BigDecimal(value.trim().replace(",", "."));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

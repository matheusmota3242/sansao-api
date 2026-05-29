package dev.m2g2.simao.model.chat.note;

import com.fasterxml.jackson.annotation.JsonTypeName;
import dev.m2g2.simao.dto.NoteDTO;
import dev.m2g2.simao.dto.chat.NoteChatResponse;
import dev.m2g2.simao.enums.ChatType;
import dev.m2g2.simao.model.chat.Interaction;
import dev.m2g2.simao.model.chat.Step;

import java.util.List;

import static dev.m2g2.simao.dto.chat.NoteChatResponse.*;

@JsonTypeName("create_note")
public class CreateNoteInteraction extends Interaction<NoteDTO> {

    public CreateNoteInteraction() {
        super();
        this.steps.addAll(List.of(
                new Step("Título da nota:"),
                new Step("Conteúdo da nota:")
        ));
        this.data = new NoteDTO();
    }

    @Override
    public NoteChatResponse processInput(String value) {
        if (value.equalsIgnoreCase(ChatType.CREATE_NOTE.getValue()))
            return proceed(this.steps.getFirst().getDescription());

        if (this.steps.getFirst().equals(getCurrentStep()))
            return executeTitleStep(value);

        return executeContentStep(value);
    }

    @Override
    public String cancelMessage() {
        return "Criação de nota cancelada.";
    }

    private NoteChatResponse executeTitleStep(String value) {
        if (value == null || value.isBlank())
            return error("Título não pode ser vazio. Tente novamente.");
        this.data.setTitle(value);
        this.steps.getFirst().setCompleted(true);
        return proceed(getCurrentStep().getDescription());
    }

    private NoteChatResponse executeContentStep(String value) {
        if (value == null || value.isBlank())
            return error("Conteúdo não pode ser vazio. Tente novamente.");
        this.data.setContent(value);
        return success("Nota cadastrada com sucesso!", data);
    }
}

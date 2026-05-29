package dev.m2g2.simao.service;

import dev.m2g2.simao.dto.NoteDTO;
import dev.m2g2.simao.enums.ChatType;
import dev.m2g2.simao.model.Note;
import dev.m2g2.simao.model.chat.ChatRecord;
import dev.m2g2.simao.model.chat.note.CreateNoteInteraction;
import dev.m2g2.simao.repository.NoteRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class NoteService implements InteractionBaseService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final NoteRepository repository;
    private final ChatRecordService chatRecordService;

    public NoteService(NoteRepository repository, ChatRecordService chatRecordService) {
        this.repository = repository;
        this.chatRecordService = chatRecordService;
    }

    public Note create(NoteDTO noteDTO) {
        Note note = new Note();
        note.setTitle(noteDTO.getTitle());
        note.setContent(noteDTO.getContent());
        LocalDateTime now = LocalDateTime.now();
        note.setCreatedAt(now);
        note.setUpdatedAt(now);
        note.setActive(true);
        return repository.save(note);
    }

    @Override
    public String createInteractionIf(String incomingMessage) {
        if (incomingMessage.equalsIgnoreCase(ChatType.CREATE_NOTE.getValue())) {
            CreateNoteInteraction interaction = new CreateNoteInteraction();
            ChatRecord record = new ChatRecord();
            record.setInteraction(interaction);
            chatRecordService.create(record);
            return interaction.processInput(incomingMessage).text();
        }
        return null;
    }

    @Override
    public String listIf(String incomingMessage) {
        if (!incomingMessage.equalsIgnoreCase(ChatType.LIST_NOTES.getValue()))
            return null;

        List<Note> notes = repository.findAllByActiveTrueOrderByCreatedAtDesc();
        if (notes.isEmpty())
            return "Você não possui notas!";

        StringBuilder builder = new StringBuilder("Aqui estão suas notas:\n\n");
        for (Note note : notes) {
            builder.append("*%d - %s* (%s)\n%s\n\n".formatted(
                    note.getId(),
                    note.getTitle(),
                    note.getCreatedAt().format(FORMATTER),
                    note.getContent()));
        }
        return builder.toString().trim();
    }

    @Override
    public String deleteIf(String incomingMessage) {
        if (!incomingMessage.toLowerCase().startsWith("@dnote"))
            return null;

        String[] parts = incomingMessage.split(" ");
        if (parts.length < 2)
            return "Uso: @dnote <id>";

        try {
            Long id = Long.parseLong(parts[1]);
            if (!repository.existsById(id))
                return "Nota com id %d não encontrada.".formatted(id);
            repository.deleteById(id);
            return "Nota com id %d removida!".formatted(id);
        } catch (NumberFormatException e) {
            return "Id inválido. Tente novamente.";
        }
    }
}

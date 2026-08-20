package dev.m2g2.simao.service;

import dev.m2g2.simao.dto.TaskDTO;
import dev.m2g2.simao.enums.ChatType;
import dev.m2g2.simao.model.task.Task;
import dev.m2g2.simao.model.chat.ChatRecord;
import dev.m2g2.simao.model.chat.task.CreateTaskInteraction;
import dev.m2g2.simao.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TaskService implements InteractionBaseService {

    private final TaskRepository repository;
    private final ChatRecordService chatRecordService;

    public TaskService(TaskRepository repository, ChatRecordService chatRecordService) {
        this.repository = repository;
        this.chatRecordService = chatRecordService;
    }

    public Task create(TaskDTO taskDTO) {
        Task task = new Task();
        task.setDescription(taskDTO.getDescription());
        task.setScheduledAt(taskDTO.getScheduledAt());
        LocalDateTime now = LocalDateTime.now();
        task.setCreatedAt(now);
        task.setUpdatedAt(now);
        task.setActive(true);
        task = repository.save(task);
        return task;
    }

    public Optional<Task> getById(Long id) {
        return repository.findById(id);
    }

    public String createInteractionIf(String message, String chatId, String participantId) {
        if (message.equalsIgnoreCase(ChatType.CREATE_TASK.getValue())) {
            CreateTaskInteraction createTaskInteraction = new CreateTaskInteraction();
            ChatRecord record = new ChatRecord();
            record.setInteraction(createTaskInteraction);
            record.setChatId(chatId);
            record.setParticipantId(participantId);
            chatRecordService.create(record);
            return createTaskInteraction.processInput(message).text();
        }
        return null;
    }

    public String listIf(String message) {
        String reply = null;
        if (message.equalsIgnoreCase(ChatType.LIST_TASKS.getValue())) {
            List<Task> tasks = repository.findAllByActiveTrue();
            if (tasks.isEmpty()) {
                reply = "Você ainda não possui tarefas!";
            } else {
                StringBuilder builder = new StringBuilder("""
                        Aqui estão suas tarefas:
                        
                        """);
                for (Task task : tasks) {
                    // scheduledAt is an optional due date, so it may be null.
                    String due = task.getScheduledAt() == null
                            ? "sem data"
                            : task.getScheduledAt().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
                    builder.append("""
                            %d - %s (%s)

                            """.formatted(task.getId(), task.getDescription(), due));
                }
                builder.append("""
                        *Ações*
                        
                        ✅ Para concluir uma tarefa digite: *@etask <task_id>*
                        ❌ Para remover uma terefa digite: *@dtask <task_id>*
                        """);
                reply = builder.toString();
            }
        }
        return reply;
    }

    public String deleteIf(String message) {
        String reply = null;
        if (message.toLowerCase().startsWith("@dtask")) {
            String[] parts = message.split(" ");
            Long taskId = Long.parseLong(parts[1]);
            repository.deleteById(taskId);
            reply = "Tarefa com id %d removida!".formatted(taskId);
        }
        return reply;
    }

    public String completeTaskIf(String message) {
        String reply = null;
        if (message.toLowerCase().startsWith(ChatType.EXECUTE_TASK.getValue())) {
            String[] parts = message.split(" ");
            Long taskId = Long.parseLong(parts[1]);
            Task task = repository.findById(taskId).orElse(null);
            if (task!= null) {
                task.setCompleted(true);
                task.setUpdatedAt(LocalDateTime.now());
                repository.save(task);
                reply = "Tarefa com id %d concluída!".formatted(taskId);
            }
        }
        return reply;
    }
}

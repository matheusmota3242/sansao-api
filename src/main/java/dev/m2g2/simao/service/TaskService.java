package dev.m2g2.simao.service;

import dev.m2g2.simao.enums.ChatType;
import dev.m2g2.simao.model.Task;
import dev.m2g2.simao.model.chat.ChatRecord;
import dev.m2g2.simao.model.chat.task.CreateTaskInteraction;
import dev.m2g2.simao.repository.TaskRepository;
import org.springframework.data.util.Pair;
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

    public Task create(Task task) {
        LocalDateTime now = LocalDateTime.now();
        task.setCreatedAt(now);
        task.setUpdatedAt(now);
        return repository.save(task);
    }

    public Optional<Task> getById(Long id) {
        return repository.findById(id);
    }

    public String createInteractionIf(String message) {
        if (message.equalsIgnoreCase(ChatType.CREATE_TASK.getValue())) {
            CreateTaskInteraction createTaskInteraction = new CreateTaskInteraction();
            ChatRecord record = new ChatRecord();
            record.setInteraction(createTaskInteraction);
            chatRecordService.create(record);
            return createTaskInteraction.processInput(message).toString();
        }
        return null;
    }

    public String listIf(String message) {
        String reply = null;
        if (message.equalsIgnoreCase(ChatType.LIST_TASKS.getValue())) {
            List<Task> tasks = repository.findAllByActiveTrue();
            if (tasks.isEmpty()) {
                reply = "You don't have any tasks yet!";
            } else {
                StringBuilder builder = new StringBuilder("Here are your tasks:\n\n");
                for (Task task : tasks) {
                    builder.append(task.getId())
                            .append(" - ")
                            .append(task.getDescription())
                            .append(" (")
                            .append(task.getScheduledAt().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
                            .append(")")
                            .append("\n");
                }
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
            reply = String.format("Task with id %d deleted!", taskId);
        }
        return reply;
    }

    public String createIf(Object pairCandidate, ChatRecord record) {
        String reply = null;
        if (pairCandidate instanceof Pair<?,?> replyAndTask) {
            record.setCompleted(true);
            reply = (String) replyAndTask.getFirst();
            if (replyAndTask.getSecond() instanceof Task task) {
                LocalDateTime now = LocalDateTime.now();
                task.setCreatedAt(now);
                task.setUpdatedAt(now);
                task.setActive(true);
                repository.save(task);
            }
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
                reply = String.format("Task with id %d marked as complete!", taskId);
            }
        }
        return reply;
    }

}

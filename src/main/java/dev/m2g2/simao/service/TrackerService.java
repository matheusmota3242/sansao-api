package dev.m2g2.simao.service;

import dev.m2g2.simao.dto.TrackerDTO;
import dev.m2g2.simao.enums.ChatType;
import dev.m2g2.simao.model.chat.ChatRecord;
import dev.m2g2.simao.model.chat.tracker.CreateTrackerInteraction;
import dev.m2g2.simao.model.tracker.Tracker;
import dev.m2g2.simao.model.tracker.TrackerEntry;
import dev.m2g2.simao.repository.TrackerEntryRepository;
import dev.m2g2.simao.repository.TrackerRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TrackerService implements InteractionBaseService {

    private final TrackerRepository repository;
    private final TrackerEntryRepository entryRepository;
    private final ChatRecordService chatRecordService;

    public TrackerService(TrackerRepository repository,
                          TrackerEntryRepository entryRepository,
                          ChatRecordService chatRecordService) {
        this.repository = repository;
        this.entryRepository = entryRepository;
        this.chatRecordService = chatRecordService;
    }

    @Override
    public String createInteractionIf(String incomingMessage, String chatId, String participantId) {
        if (incomingMessage.equalsIgnoreCase(ChatType.CREATE_TRACKER.getValue())) {
            CreateTrackerInteraction interaction = new CreateTrackerInteraction();
            ChatRecord record = new ChatRecord();
            record.setInteraction(interaction);
            record.setChatId(chatId);
            record.setParticipantId(participantId);
            chatRecordService.create(record);
            return interaction.processInput(incomingMessage).text();
        }
        return null;
    }

    /**
     * Persists a tracker built by the interaction. Keyword uniqueness can only be
     * checked against the database, so (like order creation) it is validated here
     * and a non-null return replaces the interaction's success message.
     */
    public String createFromChat(TrackerDTO dto) {
        if (repository.existsByKeyword(dto.getKeyword()))
            return "Já existe um acompanhamento com o apelido *%s*. Nada foi criado.".formatted(dto.getKeyword());
        Tracker tracker = new Tracker();
        tracker.setName(dto.getName());
        tracker.setKeyword(dto.getKeyword());
        tracker.setUnit(dto.getUnit());
        tracker.setDailyGoal(dto.getDailyGoal());
        LocalDateTime now = LocalDateTime.now();
        tracker.setCreatedAt(now);
        tracker.setUpdatedAt(now);
        tracker.setActive(true);
        repository.save(tracker);
        return null;
    }

    @Override
    public String listIf(String incomingMessage) {
        if (!incomingMessage.equalsIgnoreCase(ChatType.LIST_TRACKERS.getValue()))
            return null;

        List<Tracker> trackers = repository.findAllByActiveTrueOrderByCreatedAtDesc();
        if (trackers.isEmpty())
            return "Você ainda não possui acompanhamentos! Crie um com @ctracker.";

        StringBuilder builder = new StringBuilder("Acompanhamentos de hoje:\n\n");
        for (Tracker tracker : trackers) {
            builder.append(progressLine(tracker)).append("\n");
        }
        builder.append("\n*Ações*\n")
               .append("➕ Registrar: *@<apelido> <quantidade>* (ex: @").append(trackers.getFirst().getKeyword()).append(" 250)\n")
               .append("❌ Remover: *@dtracker <id>*");
        return builder.toString();
    }

    @Override
    public String deleteIf(String incomingMessage) {
        if (!incomingMessage.toLowerCase().startsWith("@dtracker"))
            return null;

        String[] parts = incomingMessage.split(" ");
        if (parts.length < 2)
            return "Uso: @dtracker <id>";

        try {
            Long id = Long.parseLong(parts[1]);
            Tracker tracker = repository.findById(id).orElse(null);
            if (tracker == null)
                return "Acompanhamento com id %d não encontrado.".formatted(id);
            tracker.setActive(false);
            tracker.setUpdatedAt(LocalDateTime.now());
            repository.save(tracker);
            return "Acompanhamento *%s* removido!".formatted(tracker.getName());
        } catch (NumberFormatException e) {
            return "Id inválido. Tente novamente.";
        }
    }

    /**
     * Dynamic command: "@<keyword> [amount]". Must be matched LAST in the routing
     * chain, after every static command, so a keyword can never shadow one.
     * Returns null when the token is not a known active keyword, letting the bot
     * ignore the message.
     */
    public String logIf(String incomingMessage) {
        if (incomingMessage == null || !incomingMessage.startsWith("@"))
            return null;

        String[] parts = incomingMessage.substring(1).trim().split("\\s+", 2);
        String keyword = parts[0].toLowerCase();
        Optional<Tracker> trackerOpt = repository.findByKeywordAndActiveTrue(keyword);
        if (trackerOpt.isEmpty())
            return null;

        Tracker tracker = trackerOpt.get();
        if (parts.length < 2 || parts[1].isBlank())
            return progressLine(tracker);

        BigDecimal amount = parseAmount(parts[1]);
        if (amount == null || amount.signum() <= 0)
            return "Quantidade inválida. Ex: @%s 250".formatted(keyword);

        TrackerEntry entry = new TrackerEntry();
        entry.setTracker(tracker);
        entry.setAmount(amount);
        LocalDateTime now = LocalDateTime.now();
        entry.setRecordedAt(now);
        entry.setCreatedAt(now);
        entry.setUpdatedAt(now);
        entry.setActive(true);
        entryRepository.save(entry);

        return progressLine(tracker);
    }

    private String progressLine(Tracker tracker) {
        BigDecimal total = todayTotal(tracker);
        BigDecimal goal = tracker.getDailyGoal();
        String base = "*%s*: %s/%s %s".formatted(
                tracker.getName(), format(total), format(goal), tracker.getUnit());
        if (total.compareTo(goal) >= 0)
            return base + " ✅ meta batida!";
        BigDecimal remaining = goal.subtract(total);
        return base + " (faltam %s %s)".formatted(format(remaining), tracker.getUnit());
    }

    private BigDecimal todayTotal(Tracker tracker) {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();
        BigDecimal total = entryRepository.sumAmountForPeriod(tracker.getId(), start, end);
        return total == null ? BigDecimal.ZERO : total;
    }

    private static String format(BigDecimal value) {
        if (value == null || value.signum() == 0)
            return "0";
        return value.stripTrailingZeros().toPlainString();
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

package dev.m2g2.simao.service;

import dev.m2g2.simao.dto.PurchaseDTO;
import dev.m2g2.simao.enums.ChatType;
import dev.m2g2.simao.model.Purchase;
import dev.m2g2.simao.model.chat.ChatRecord;
import dev.m2g2.simao.model.chat.purchase.CreatePurchaseInteraction;
import dev.m2g2.simao.model.chat.purchase.UpdatePurchaseInteraction;
import dev.m2g2.simao.repository.PurchaseRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PurchaseService implements InteractionBaseService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final PurchaseRepository repository;
    private final ChatRecordService chatRecordService;

    public PurchaseService(PurchaseRepository repository, ChatRecordService chatRecordService) {
        this.repository = repository;
        this.chatRecordService = chatRecordService;
    }

    public Purchase create(PurchaseDTO dto) {
        Purchase purchase = new Purchase();
        purchase.setDescription(dto.getDescription());
        purchase.setAmount(dto.getAmount());
        purchase.setUnitPrice(dto.getUnitPrice());
        purchase.setSource(dto.getSource());
        purchase.setObservations(dto.getObservations());
        LocalDateTime now = LocalDateTime.now();
        purchase.setCreatedAt(now);
        purchase.setUpdatedAt(now);
        purchase.setActive(true);
        return repository.save(purchase);
    }

    public Purchase update(Long id, PurchaseDTO dto) {
        Purchase purchase = repository.findById(id).orElse(null);
        if (purchase == null)
            return null;
        purchase.setDescription(dto.getDescription());
        purchase.setAmount(dto.getAmount());
        purchase.setUnitPrice(dto.getUnitPrice());
        purchase.setSource(dto.getSource());
        purchase.setObservations(dto.getObservations());
        return repository.save(purchase);
    }

    @Override
    public String createInteractionIf(String incomingMessage) {
        if (incomingMessage.equalsIgnoreCase(ChatType.CREATE_PURCHASE.getValue())) {
            CreatePurchaseInteraction interaction = new CreatePurchaseInteraction();
            ChatRecord record = new ChatRecord();
            record.setInteraction(interaction);
            chatRecordService.create(record);
            return interaction.processInput(incomingMessage).text();
        }
        return null;
    }

    public String updateInteractionIf(String incomingMessage) {
        if (!incomingMessage.toLowerCase().startsWith(ChatType.UPDATE_PURCHASE.getValue()))
            return null;

        String[] parts = incomingMessage.trim().split("\\s+");
        if (parts.length < 2)
            return "Uso: @ubuy <id>";

        Long id;
        try {
            id = Long.parseLong(parts[1]);
        } catch (NumberFormatException e) {
            return "Id inválido. Tente novamente.";
        }

        Purchase purchase = repository.findById(id).filter(Purchase::isActive).orElse(null);
        if (purchase == null)
            return "Compra com id %d não encontrada.".formatted(id);

        UpdatePurchaseInteraction interaction = new UpdatePurchaseInteraction();
        interaction.setTargetId(id);
        interaction.setData(toDto(purchase));
        ChatRecord record = new ChatRecord();
        record.setInteraction(interaction);
        chatRecordService.create(record);
        return interaction.processInput(ChatType.UPDATE_PURCHASE.getValue()).text();
    }

    @Override
    public String listIf(String incomingMessage) {
        if (!incomingMessage.equalsIgnoreCase(ChatType.LIST_PURCHASES.getValue()))
            return null;

        List<Purchase> purchases = repository.findAllByActiveTrueOrderByCreatedAtDesc();
        if (purchases.isEmpty())
            return "Nenhuma compra registrada!";

        StringBuilder builder = new StringBuilder("Compras de insumos 3D:\n\n");
        for (Purchase purchase : purchases) {
            BigDecimal total = purchase.getUnitPrice().multiply(BigDecimal.valueOf(purchase.getAmount()));
            builder.append("*%d - %s* (%s)\n".formatted(
                    purchase.getId(),
                    purchase.getDescription(),
                    purchase.getCreatedAt().format(FORMATTER)));
            builder.append("Qtd: %d | Unit.: R$ %s | Total: R$ %s\n".formatted(
                    purchase.getAmount(),
                    purchase.getUnitPrice().toPlainString(),
                    total.toPlainString()));
            builder.append("Fonte: %s\n".formatted(purchase.getSource()));
            if (purchase.getObservations() != null && !purchase.getObservations().isBlank())
                builder.append("Obs: %s\n".formatted(purchase.getObservations()));
            builder.append("\n");
        }
        return builder.toString().trim();
    }

    @Override
    public String deleteIf(String incomingMessage) {
        if (!incomingMessage.toLowerCase().startsWith(ChatType.DELETE_PURCHASE.getValue()))
            return null;

        String[] parts = incomingMessage.trim().split("\\s+");
        if (parts.length < 2)
            return "Uso: @dbuy <id>";

        try {
            Long id = Long.parseLong(parts[1]);
            if (!repository.existsById(id))
                return "Compra com id %d não encontrada.".formatted(id);
            repository.deleteById(id);
            return "Compra com id %d removida!".formatted(id);
        } catch (NumberFormatException e) {
            return "Id inválido. Tente novamente.";
        }
    }

    private PurchaseDTO toDto(Purchase purchase) {
        PurchaseDTO dto = new PurchaseDTO();
        dto.setDescription(purchase.getDescription());
        dto.setAmount(purchase.getAmount());
        dto.setUnitPrice(purchase.getUnitPrice());
        dto.setSource(purchase.getSource());
        dto.setObservations(purchase.getObservations());
        return dto;
    }
}

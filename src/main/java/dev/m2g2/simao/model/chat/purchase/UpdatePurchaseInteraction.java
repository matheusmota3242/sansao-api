package dev.m2g2.simao.model.chat.purchase;

import com.fasterxml.jackson.annotation.JsonTypeName;
import dev.m2g2.simao.dto.PurchaseDTO;
import dev.m2g2.simao.dto.chat.PurchaseChatResponse;
import dev.m2g2.simao.enums.ChatType;
import dev.m2g2.simao.model.chat.Interaction;
import dev.m2g2.simao.model.chat.Step;
import dev.m2g2.simao.util.PurchaseInputUtil;

import java.util.List;

import static dev.m2g2.simao.dto.chat.PurchaseChatResponse.*;

@JsonTypeName("update_purchase")
public class UpdatePurchaseInteraction extends Interaction<PurchaseDTO> {

    private static final String FIELD_SELECTION_PROMPT = """
            Qual campo deseja atualizar?

            1 - Descrição
            2 - Quantidade
            3 - Preço unitário
            4 - Fonte
            5 - Data da compra
            6 - Observações
            """;

    private Long targetId;
    private String field;

    public UpdatePurchaseInteraction() {
        super();
        this.steps.addAll(List.of(
                new Step(FIELD_SELECTION_PROMPT),
                new Step("Novo valor:")
        ));
        this.data = new PurchaseDTO();
    }

    @Override
    public PurchaseChatResponse processInput(String value) {
        if (value != null && value.toLowerCase().startsWith(ChatType.UPDATE_PURCHASE.getValue()))
            return proceed(this.steps.getFirst().getDescription());

        if (this.steps.get(0).equals(getCurrentStep()))
            return executeFieldStep(value);

        return executeValueStep(value);
    }

    @Override
    public String cancelMessage() {
        return "Atualização de compra cancelada.";
    }

    private PurchaseChatResponse executeFieldStep(String value) {
        String selected = switch (value == null ? "" : value.trim()) {
            case "1" -> "description";
            case "2" -> "amount";
            case "3" -> "unitPrice";
            case "4" -> "source";
            case "5" -> "purchasedAt";
            case "6" -> "observations";
            default -> null;
        };
        if (selected == null)
            return error("Opção inválida. Escolha um número de 1 a 6.");
        this.field = selected;
        this.steps.get(0).setCompleted(true);
        return proceed(getCurrentStep().getDescription());
    }

    private PurchaseChatResponse executeValueStep(String value) {
        switch (this.field) {
            case "description" -> {
                if (value == null || value.isBlank())
                    return error("Descrição não pode ser vazia. Tente novamente.");
                this.data.setDescription(value.trim());
            }
            case "amount" -> {
                try {
                    this.data.setAmount(PurchaseInputUtil.parseAmount(value));
                } catch (IllegalArgumentException e) {
                    return error("Quantidade inválida. Informe um número inteiro positivo.");
                }
            }
            case "unitPrice" -> {
                try {
                    this.data.setUnitPrice(PurchaseInputUtil.parsePrice(value));
                } catch (IllegalArgumentException e) {
                    return error("Preço inválido. Ex: 49,90");
                }
            }
            case "source" -> {
                if (value == null || value.isBlank())
                    return error("Fonte não pode ser vazia. Tente novamente.");
                this.data.setSource(value.trim());
            }
            case "purchasedAt" -> {
                try {
                    this.data.setPurchasedAt(PurchaseInputUtil.parsePurchasedAt(value));
                } catch (IllegalArgumentException e) {
                    return error("Data inválida. Use o formato dd/mm/aaaa. Ex: 04/08/2026");
                }
            }
            case "observations" -> this.data.setObservations(
                    value == null || value.isBlank() ? null : value.trim());
            default -> {
                return error("Campo inválido.");
            }
        }
        return updated("Compra atualizada com sucesso!", data, targetId);
    }

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }
}

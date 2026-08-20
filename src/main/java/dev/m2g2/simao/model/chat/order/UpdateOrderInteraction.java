package dev.m2g2.simao.model.chat.order;

import com.fasterxml.jackson.annotation.JsonTypeName;
import dev.m2g2.simao.dto.OrderDTO;
import dev.m2g2.simao.dto.chat.OrderChatResponse;
import dev.m2g2.simao.enums.ChatType;
import dev.m2g2.simao.model.chat.Interaction;
import dev.m2g2.simao.model.chat.Step;
import dev.m2g2.simao.util.OrderInputUtil;

import java.util.List;

import static dev.m2g2.simao.dto.chat.OrderChatResponse.error;
import static dev.m2g2.simao.dto.chat.OrderChatResponse.proceed;
import static dev.m2g2.simao.dto.chat.OrderChatResponse.updated;

@JsonTypeName("update_order")
public class UpdateOrderInteraction extends Interaction<OrderDTO> {

    private static final String SKIP = "-";

    private static final String FIELD_SELECTION_PROMPT = """
            Qual campo deseja atualizar?

            1 - Descrição
            2 - Cliente (nome ou id)
            3 - Tempo de impressão
            4 - Custo de produção
            5 - Preço de venda
            6 - Observações
            """;

    private Long targetId;
    private String field;

    public UpdateOrderInteraction() {
        super();
        this.steps.addAll(List.of(
                new Step(FIELD_SELECTION_PROMPT),
                new Step("Novo valor:")
        ));
        this.data = new OrderDTO();
    }

    @Override
    public OrderChatResponse processInput(String value) {
        if (value != null && value.toLowerCase().startsWith(ChatType.UPDATE_ORDER.getValue()))
            return proceed(this.steps.getFirst().getDescription());

        // Index derived from completed flags, not Step identity — see
        // CreateOrderInteraction for why.
        if (!this.steps.getFirst().isCompleted())
            return executeFieldStep(value);

        return executeValueStep(value);
    }

    @Override
    public String cancelMessage() {
        return "Atualização de pedido cancelada.";
    }

    private OrderChatResponse executeFieldStep(String value) {
        String selected = switch (value == null ? "" : value.trim()) {
            case "1" -> "description";
            case "2" -> "customerName";
            case "3" -> "printTimeMinutes";
            case "4" -> "productionCost";
            case "5" -> "salePrice";
            case "6" -> "observations";
            default -> null;
        };
        if (selected == null)
            return error("Opção inválida. Escolha um número de 1 a 6.");
        this.field = selected;
        this.steps.getFirst().setCompleted(true);
        return proceed(getCurrentStep().getDescription());
    }

    private OrderChatResponse executeValueStep(String value) {
        switch (this.field) {
            case "description" -> {
                if (value == null || value.isBlank())
                    return error("Descrição não pode ser vazia. Tente novamente.");
                this.data.setDescription(value.trim());
            }
            case "customerName" -> {
                if (value == null || value.isBlank())
                    return error("Cliente não pode ser vazio. Informe o nome ou o id.");
                this.data.setCustomerName(value.trim());
            }
            case "printTimeMinutes" -> {
                if (isSkip(value)) {
                    this.data.setPrintTimeMinutes(null);
                } else {
                    try {
                        this.data.setPrintTimeMinutes(OrderInputUtil.parsePrintTimeMinutes(value));
                    } catch (IllegalArgumentException e) {
                        return error("Tempo inválido. Use 4h30, 4:30 ou o total em minutos (90).");
                    }
                }
            }
            case "productionCost" -> {
                if (isSkip(value)) {
                    this.data.setProductionCost(null);
                } else {
                    try {
                        this.data.setProductionCost(OrderInputUtil.parsePrice(value));
                    } catch (IllegalArgumentException e) {
                        return error("Custo inválido. Ex: 12,50");
                    }
                }
            }
            case "salePrice" -> {
                if (isSkip(value)) {
                    this.data.setSalePrice(null);
                } else {
                    try {
                        this.data.setSalePrice(OrderInputUtil.parsePrice(value));
                    } catch (IllegalArgumentException e) {
                        return error("Preço inválido. Ex: 49,90");
                    }
                }
            }
            case "observations" -> this.data.setObservations(isSkip(value) ? null : value.trim());
            default -> {
                return error("Campo inválido.");
            }
        }
        this.steps.getLast().setCompleted(true);
        return updated("Pedido atualizado com sucesso!", data, targetId);
    }

    private boolean isSkip(String value) {
        return value == null || value.isBlank() || value.trim().equals(SKIP);
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

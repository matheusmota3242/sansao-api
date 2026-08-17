package dev.m2g2.simao.model.chat.order;

import com.fasterxml.jackson.annotation.JsonTypeName;
import dev.m2g2.simao.dto.OrderDTO;
import dev.m2g2.simao.dto.chat.OrderChatResponse;
import dev.m2g2.simao.enums.ChatType;
import dev.m2g2.simao.model.chat.Interaction;
import dev.m2g2.simao.model.chat.Step;
import dev.m2g2.simao.util.OrderInputUtil;

import java.util.List;

import static dev.m2g2.simao.dto.chat.OrderChatResponse.created;
import static dev.m2g2.simao.dto.chat.OrderChatResponse.error;
import static dev.m2g2.simao.dto.chat.OrderChatResponse.proceed;

@JsonTypeName("create_order")
public class CreateOrderInteraction extends Interaction<OrderDTO> {

    protected static final String SKIP = "-";

    public CreateOrderInteraction() {
        super();
        this.steps.addAll(List.of(
                new Step("Descrição do pedido:"),
                new Step("Cliente (nome ou id — veja @lcli):"),
                new Step("Tempo de impressão (ex: 4h30 ou 90; - para pular):"),
                new Step("Custo de produção (R$; - para pular):"),
                new Step("Preço de venda (R$; - para pular):"),
                new Step("Observações (digite - para pular):")
        ));
        this.data = new OrderDTO();
    }

    @Override
    public OrderChatResponse processInput(String value) {
        if (value.equalsIgnoreCase(ChatType.CREATE_ORDER.getValue()))
            return proceed(this.steps.getFirst().getDescription());

        // Steps are identified by index derived from the completed flags rather
        // than by comparing Step instances: Step has no equals(), so object
        // identity does not survive the ChatRecord round-trip through Jackson.
        return switch (currentStepIndex()) {
            case 0 -> executeDescriptionStep(value);
            case 1 -> executeCustomerStep(value);
            case 2 -> executePrintTimeStep(value);
            case 3 -> executeProductionCostStep(value);
            case 4 -> executeSalePriceStep(value);
            default -> executeObservationsStep(value);
        };
    }

    @Override
    public String cancelMessage() {
        return "Registro de pedido cancelado.";
    }

    protected int currentStepIndex() {
        for (int i = 0; i < this.steps.size(); i++) {
            if (!this.steps.get(i).isCompleted())
                return i;
        }
        return this.steps.size() - 1;
    }

    protected OrderChatResponse completeStep(int index) {
        this.steps.get(index).setCompleted(true);
        return proceed(getCurrentStep().getDescription());
    }

    private OrderChatResponse executeDescriptionStep(String value) {
        if (value == null || value.isBlank())
            return error("Descrição não pode ser vazia. Tente novamente.");
        this.data.setDescription(value.trim());
        return completeStep(0);
    }

    private OrderChatResponse executeCustomerStep(String value) {
        if (value == null || value.isBlank())
            return error("Cliente não pode ser vazio. Informe o nome ou o id.");
        this.data.setCustomerName(value.trim());
        return completeStep(1);
    }

    private OrderChatResponse executePrintTimeStep(String value) {
        if (isSkip(value)) {
            this.data.setPrintTimeMinutes(null);
            return completeStep(2);
        }
        try {
            this.data.setPrintTimeMinutes(OrderInputUtil.parsePrintTimeMinutes(value));
        } catch (IllegalArgumentException e) {
            return error("Tempo inválido. Use 4h30, 4:30 ou o total em minutos (90).");
        }
        return completeStep(2);
    }

    private OrderChatResponse executeProductionCostStep(String value) {
        if (isSkip(value)) {
            this.data.setProductionCost(null);
            return completeStep(3);
        }
        try {
            this.data.setProductionCost(OrderInputUtil.parsePrice(value));
        } catch (IllegalArgumentException e) {
            return error("Custo inválido. Ex: 12,50");
        }
        return completeStep(3);
    }

    private OrderChatResponse executeSalePriceStep(String value) {
        if (isSkip(value)) {
            this.data.setSalePrice(null);
            return completeStep(4);
        }
        try {
            this.data.setSalePrice(OrderInputUtil.parsePrice(value));
        } catch (IllegalArgumentException e) {
            return error("Preço inválido. Ex: 49,90");
        }
        return completeStep(4);
    }

    private OrderChatResponse executeObservationsStep(String value) {
        if (!isSkip(value))
            this.data.setObservations(value.trim());
        this.steps.getLast().setCompleted(true);
        return created("Pedido registrado com sucesso!", data);
    }

    protected boolean isSkip(String value) {
        return value == null || value.isBlank() || value.trim().equals(SKIP);
    }
}

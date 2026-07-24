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

@JsonTypeName("create_purchase")
public class CreatePurchaseInteraction extends Interaction<PurchaseDTO> {

    private static final String SKIP = "-";

    public CreatePurchaseInteraction() {
        super();
        this.steps.addAll(List.of(
                new Step("Descrição da compra:"),
                new Step("Quantidade:"),
                new Step("Preço unitário (R$):"),
                new Step("Fonte/loja:"),
                new Step("Observações (digite - para pular):")
        ));
        this.data = new PurchaseDTO();
    }

    @Override
    public PurchaseChatResponse processInput(String value) {
        if (value.equalsIgnoreCase(ChatType.CREATE_PURCHASE.getValue()))
            return proceed(this.steps.getFirst().getDescription());

        if (this.steps.get(0).equals(getCurrentStep()))
            return executeDescriptionStep(value);

        if (this.steps.get(1).equals(getCurrentStep()))
            return executeAmountStep(value);

        if (this.steps.get(2).equals(getCurrentStep()))
            return executeUnitPriceStep(value);

        if (this.steps.get(3).equals(getCurrentStep()))
            return executeSourceStep(value);

        return executeObservationsStep(value);
    }

    @Override
    public String cancelMessage() {
        return "Registro de compra cancelado.";
    }

    private PurchaseChatResponse executeDescriptionStep(String value) {
        if (value == null || value.isBlank())
            return error("Descrição não pode ser vazia. Tente novamente.");
        this.data.setDescription(value.trim());
        this.steps.get(0).setCompleted(true);
        return proceed(getCurrentStep().getDescription());
    }

    private PurchaseChatResponse executeAmountStep(String value) {
        try {
            this.data.setAmount(PurchaseInputUtil.parseAmount(value));
        } catch (IllegalArgumentException e) {
            return error("Quantidade inválida. Informe um número inteiro positivo.");
        }
        this.steps.get(1).setCompleted(true);
        return proceed(getCurrentStep().getDescription());
    }

    private PurchaseChatResponse executeUnitPriceStep(String value) {
        try {
            this.data.setUnitPrice(PurchaseInputUtil.parsePrice(value));
        } catch (IllegalArgumentException e) {
            return error("Preço inválido. Ex: 49,90");
        }
        this.steps.get(2).setCompleted(true);
        return proceed(getCurrentStep().getDescription());
    }

    private PurchaseChatResponse executeSourceStep(String value) {
        if (value == null || value.isBlank())
            return error("Fonte não pode ser vazia. Tente novamente.");
        this.data.setSource(value.trim());
        this.steps.get(3).setCompleted(true);
        return proceed(getCurrentStep().getDescription());
    }

    private PurchaseChatResponse executeObservationsStep(String value) {
        if (value != null && !value.isBlank() && !value.trim().equals(SKIP))
            this.data.setObservations(value.trim());
        return created("Compra registrada com sucesso!", data);
    }
}

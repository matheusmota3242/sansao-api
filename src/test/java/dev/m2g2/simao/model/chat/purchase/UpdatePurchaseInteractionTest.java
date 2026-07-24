package dev.m2g2.simao.model.chat.purchase;

import dev.m2g2.simao.dto.PurchaseDTO;
import dev.m2g2.simao.dto.chat.PurchaseChatResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class UpdatePurchaseInteractionTest {

    private UpdatePurchaseInteraction interaction;

    @BeforeEach
    void setUp() {
        interaction = new UpdatePurchaseInteraction();
        interaction.setTargetId(7L);
        interaction.setData(seededDto());
    }

    @Test
    void initialTrigger_returnsFieldSelectionPrompt() {
        PurchaseChatResponse response = interaction.processInput("@ubuy");

        assertFalse(response.completed());
        assertTrue(response.text().contains("Qual campo"));
    }

    @Test
    void updatingAmount_changesOnlyAmountAndKeepsOtherFields() {
        interaction.processInput("@ubuy");
        interaction.processInput("2"); // amount

        PurchaseChatResponse response = interaction.processInput("10");

        assertTrue(response.completed());
        assertEquals(7L, response.updateId());
        assertEquals(10, response.purchase().getAmount());
        // untouched fields keep the seeded values
        assertEquals("Filamento PLA preto", response.purchase().getDescription());
        assertEquals(0, new BigDecimal("89.90").compareTo(response.purchase().getUnitPrice()));
        assertEquals("Loja 3D Fila", response.purchase().getSource());
    }

    @Test
    void updatingUnitPrice_parsesBrazilianFormat() {
        interaction.processInput("@ubuy");
        interaction.processInput("3"); // unit price

        PurchaseChatResponse response = interaction.processInput("120,00");

        assertTrue(response.completed());
        assertEquals(0, new BigDecimal("120.00").compareTo(response.purchase().getUnitPrice()));
    }

    @Test
    void invalidFieldOption_returnsErrorAndStaysOnStep() {
        interaction.processInput("@ubuy");

        PurchaseChatResponse error = interaction.processInput("9");

        assertFalse(error.completed());
        assertTrue(error.text().toLowerCase().contains("inválida"));

        // valid retry proceeds to the value step
        PurchaseChatResponse retry = interaction.processInput("1");
        assertFalse(retry.completed());
        assertTrue(retry.text().contains("Novo valor"));
    }

    @Test
    void invalidAmountValue_returnsErrorAndStaysOnStep() {
        interaction.processInput("@ubuy");
        interaction.processInput("2"); // amount

        PurchaseChatResponse error = interaction.processInput("muitos");

        assertFalse(error.completed());
        assertNull(error.updateId());

        // valid retry completes
        PurchaseChatResponse retry = interaction.processInput("5");
        assertTrue(retry.completed());
        assertEquals(5, retry.purchase().getAmount());
    }

    @Test
    void cancelMessage_returnsExpectedText() {
        assertEquals("Atualização de compra cancelada.", interaction.cancelMessage());
    }

    private PurchaseDTO seededDto() {
        PurchaseDTO dto = new PurchaseDTO();
        dto.setDescription("Filamento PLA preto");
        dto.setAmount(3);
        dto.setUnitPrice(new BigDecimal("89.90"));
        dto.setSource("Loja 3D Fila");
        dto.setObservations(null);
        return dto;
    }
}

package dev.m2g2.simao.model.chat.order;

import dev.m2g2.simao.dto.chat.OrderChatResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CreateOrderInteractionTest {

    private CreateOrderInteraction interaction;

    @BeforeEach
    void setUp() {
        interaction = new CreateOrderInteraction();
    }

    @Test
    void initialTrigger_returnsDescriptionStepPrompt() {
        OrderChatResponse response = interaction.processInput("@cord");

        assertFalse(response.completed());
        assertNull(response.order());
        assertTrue(response.text().contains("Descrição"));
    }

    @Test
    void happyPath_completesWithPopulatedOrder() {
        interaction.processInput("@cord");
        interaction.processInput("Suporte de headset");
        interaction.processInput("Rodrigo");
        interaction.processInput("4h30");
        interaction.processInput("12,50");
        interaction.processInput("60,00");

        OrderChatResponse response = interaction.processInput("Cor preta");

        assertTrue(response.completed());
        assertNotNull(response.order());
        assertNull(response.updateId());
        assertEquals("Suporte de headset", response.order().getDescription());
        assertEquals("Rodrigo", response.order().getCustomerName());
        assertEquals(270, response.order().getPrintTimeMinutes());
        assertEquals(0, new BigDecimal("12.50").compareTo(response.order().getProductionCost()));
        assertEquals(0, new BigDecimal("60.00").compareTo(response.order().getSalePrice()));
        assertEquals("Cor preta", response.order().getObservations());
    }

    @Test
    void optionalFields_skippedWithDash_stayNull() {
        interaction.processInput("@cord");
        interaction.processInput("Peça de reposição");
        interaction.processInput("Maria");
        interaction.processInput("-");
        interaction.processInput("-");
        interaction.processInput("-");

        OrderChatResponse response = interaction.processInput("-");

        assertTrue(response.completed());
        assertNull(response.order().getPrintTimeMinutes());
        assertNull(response.order().getProductionCost());
        assertNull(response.order().getSalePrice());
        assertNull(response.order().getObservations());
        assertEquals("Peça de reposição", response.order().getDescription());
        assertEquals("Maria", response.order().getCustomerName());
    }

    @Test
    void blankDescription_doesNotAdvanceStep() {
        interaction.processInput("@cord");

        OrderChatResponse response = interaction.processInput("   ");

        assertFalse(response.completed());
        assertTrue(response.text().contains("não pode ser vazia"));
        // Error responses must not mark the step completed.
        assertFalse(interaction.getSteps().getFirst().isCompleted());
    }

    @Test
    void blankCustomerName_doesNotAdvanceStep() {
        interaction.processInput("@cord");
        interaction.processInput("Vaso decorativo");

        OrderChatResponse response = interaction.processInput("  ");

        assertFalse(response.completed());
        assertTrue(response.text().contains("Cliente"));
        assertFalse(interaction.getSteps().get(1).isCompleted());
    }

    @Test
    void customerStep_keepsNumericInputVerbatimForIdLookup() {
        interaction.processInput("@cord");
        interaction.processInput("Miniatura");
        interaction.processInput("7");
        interaction.processInput("-");
        interaction.processInput("-");
        interaction.processInput("-");

        OrderChatResponse response = interaction.processInput("-");

        assertTrue(response.completed());
        // The interaction has no database access: it stores the raw input and
        // CustomerService decides whether "7" is an id or a name.
        assertEquals("7", response.order().getCustomerName());
    }

    @Test
    void invalidPrintTime_doesNotAdvanceStep() {
        interaction.processInput("@cord");
        interaction.processInput("Engrenagem");
        interaction.processInput("Rodrigo");

        OrderChatResponse response = interaction.processInput("depois eu vejo");

        assertFalse(response.completed());
        assertTrue(response.text().contains("Tempo inválido"));
        assertFalse(interaction.getSteps().get(2).isCompleted());
    }

    @Test
    void invalidPrice_doesNotAdvanceStep() {
        interaction.processInput("@cord");
        interaction.processInput("Engrenagem");
        interaction.processInput("Rodrigo");
        interaction.processInput("90");
        interaction.processInput("10,00");

        OrderChatResponse response = interaction.processInput("caro");

        assertFalse(response.completed());
        assertTrue(response.text().contains("Preço inválido"));
        assertFalse(interaction.getSteps().get(4).isCompleted());
    }

    @Test
    void printTimeAcceptsPlainMinutes() {
        interaction.processInput("@cord");
        interaction.processInput("Chaveiro");
        interaction.processInput("João");
        interaction.processInput("45");
        interaction.processInput("-");
        interaction.processInput("-");

        OrderChatResponse response = interaction.processInput("-");

        assertTrue(response.completed());
        assertEquals(45, response.order().getPrintTimeMinutes());
    }

    @Test
    void cancelMessage_isProvided() {
        assertNotNull(interaction.cancelMessage());
        assertTrue(interaction.cancelMessage().toLowerCase().contains("cancelado"));
    }
}

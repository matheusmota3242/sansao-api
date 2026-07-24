package dev.m2g2.simao.model.chat.purchase;

import dev.m2g2.simao.dto.chat.PurchaseChatResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class CreatePurchaseInteractionTest {

    private CreatePurchaseInteraction interaction;

    @BeforeEach
    void setUp() {
        interaction = new CreatePurchaseInteraction();
    }

    @Test
    void initialTrigger_returnsDescriptionStepPrompt() {
        PurchaseChatResponse response = interaction.processInput("@cbuy");

        assertFalse(response.completed());
        assertNull(response.purchase());
        assertTrue(response.text().contains("Descrição"));
    }

    @Test
    void happyPath_completesWithPopulatedPurchase() {
        interaction.processInput("@cbuy");
        interaction.processInput("Filamento PLA preto");
        interaction.processInput("3");
        interaction.processInput("89,90");
        interaction.processInput("Loja 3D Fila");

        PurchaseChatResponse response = interaction.processInput("Promoção");

        assertTrue(response.completed());
        assertNotNull(response.purchase());
        assertNull(response.updateId());
        assertEquals("Filamento PLA preto", response.purchase().getDescription());
        assertEquals(3, response.purchase().getAmount());
        assertEquals(0, new BigDecimal("89.90").compareTo(response.purchase().getUnitPrice()));
        assertEquals("Loja 3D Fila", response.purchase().getSource());
        assertEquals("Promoção", response.purchase().getObservations());
    }

    @Test
    void observationsStep_skipWithDash_leavesObservationsNull() {
        driveToObservationsStep();

        PurchaseChatResponse response = interaction.processInput("-");

        assertTrue(response.completed());
        assertNull(response.purchase().getObservations());
    }

    @Test
    void amountStep_nonNumeric_returnsErrorAndStaysOnStep() {
        interaction.processInput("@cbuy");
        interaction.processInput("Bico 0.4mm");

        PurchaseChatResponse error = interaction.processInput("dois");

        assertFalse(error.completed());
        assertNull(error.purchase());
        assertTrue(error.text().toLowerCase().contains("quantidade"));

        // valid retry advances to the price step
        PurchaseChatResponse retry = interaction.processInput("2");
        assertFalse(retry.completed());
        assertTrue(retry.text().contains("Preço"));
    }

    @Test
    void amountStep_zeroOrNegative_returnsError() {
        interaction.processInput("@cbuy");
        interaction.processInput("Bico 0.4mm");

        PurchaseChatResponse response = interaction.processInput("0");

        assertFalse(response.completed());
        assertNull(response.purchase());
    }

    @Test
    void unitPriceStep_invalid_returnsErrorAndStaysOnStep() {
        interaction.processInput("@cbuy");
        interaction.processInput("Bico 0.4mm");
        interaction.processInput("2");

        PurchaseChatResponse error = interaction.processInput("abc");

        assertFalse(error.completed());
        assertNull(error.purchase());
        assertTrue(error.text().toLowerCase().contains("preço"));

        // dot decimal is accepted and advances to the source step
        PurchaseChatResponse retry = interaction.processInput("15.50");
        assertFalse(retry.completed());
        assertTrue(retry.text().contains("Fonte"));
    }

    @Test
    void cancelMessage_returnsExpectedText() {
        assertEquals("Registro de compra cancelado.", interaction.cancelMessage());
    }

    private void driveToObservationsStep() {
        interaction.processInput("@cbuy");
        interaction.processInput("Filamento PLA preto");
        interaction.processInput("1");
        interaction.processInput("100");
        interaction.processInput("Loja 3D Fila");
    }
}

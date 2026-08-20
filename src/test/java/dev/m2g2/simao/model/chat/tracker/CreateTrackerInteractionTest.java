package dev.m2g2.simao.model.chat.tracker;

import dev.m2g2.simao.dto.chat.TrackerChatResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class CreateTrackerInteractionTest {

    private CreateTrackerInteraction interaction;

    @BeforeEach
    void setUp() {
        interaction = new CreateTrackerInteraction();
    }

    // -------------------------------------------------------------------------
    // Initial trigger
    // -------------------------------------------------------------------------

    @Test
    void initialTrigger_returnsNameStepPrompt() {
        TrackerChatResponse response = interaction.processInput("@ctracker");

        assertFalse(response.completed());
        assertNull(response.tracker());
        assertTrue(response.text().contains("Nome do acompanhamento"));
    }

    // -------------------------------------------------------------------------
    // Happy path
    // -------------------------------------------------------------------------

    @Test
    void fullFlow_capturesAllFieldsAndCompletes() {
        interaction.processInput("@ctracker");

        assertTrue(interaction.processInput("Água").text().contains("Apelido"));
        assertTrue(interaction.processInput("agua").text().contains("Unidade"));
        assertTrue(interaction.processInput("ml").text().contains("Meta"));

        TrackerChatResponse response = interaction.processInput("2000");

        assertTrue(response.completed());
        assertNotNull(response.tracker());
        assertEquals("Água", response.tracker().getName());
        assertEquals("agua", response.tracker().getKeyword());
        assertEquals("ml", response.tracker().getUnit());
        assertEquals(0, new BigDecimal("2000").compareTo(response.tracker().getDailyGoal()));
    }

    @Test
    void goalStep_acceptsDecimalWithComma() {
        driveToGoalStep("Proteína", "prot", "g");

        TrackerChatResponse response = interaction.processInput("150,5");

        assertTrue(response.completed());
        assertEquals(0, new BigDecimal("150.5").compareTo(response.tracker().getDailyGoal()));
    }

    // -------------------------------------------------------------------------
    // Name step validation
    // -------------------------------------------------------------------------

    @Test
    void nameStep_blank_returnsErrorAndStaysOnStep() {
        interaction.processInput("@ctracker");

        TrackerChatResponse response = interaction.processInput("   ");

        assertFalse(response.completed());
        assertNull(response.tracker());
        assertTrue(response.text().toLowerCase().contains("vazio"));

        // A valid retry advances to the keyword step
        assertTrue(interaction.processInput("Água").text().contains("Apelido"));
    }

    // -------------------------------------------------------------------------
    // Keyword step validation
    // -------------------------------------------------------------------------

    @Test
    void keywordStep_withSpace_returnsErrorAndStaysOnStep() {
        driveToKeywordStep("Água");

        TrackerChatResponse response = interaction.processInput("agua fria");

        assertFalse(response.completed());
        assertTrue(response.text().toLowerCase().contains("apelido inválido"));

        // Valid retry advances to the unit step
        assertTrue(interaction.processInput("agua").text().contains("Unidade"));
    }

    @Test
    void keywordStep_startingWithDigit_returnsError() {
        driveToKeywordStep("Água");

        TrackerChatResponse response = interaction.processInput("1agua");

        assertFalse(response.completed());
        assertTrue(response.text().toLowerCase().contains("apelido inválido"));
    }

    @Test
    void keywordStep_isCaseInsensitiveAndNormalizedToLowercase() {
        driveToGoalStep("Água", "AGUA", "ml");

        TrackerChatResponse response = interaction.processInput("2000");

        assertTrue(response.completed());
        assertEquals("agua", response.tracker().getKeyword());
    }

    @Test
    void keywordStep_collidingWithStaticCommand_returnsErrorAndStaysOnStep() {
        driveToKeywordStep("Lista");

        // "ltask" would collide with the @ltask command
        TrackerChatResponse response = interaction.processInput("ltask");

        assertFalse(response.completed());
        assertTrue(response.text().toLowerCase().contains("conflita"));

        // Non-colliding retry advances
        assertTrue(interaction.processInput("agua").text().contains("Unidade"));
    }

    // -------------------------------------------------------------------------
    // Unit step validation
    // -------------------------------------------------------------------------

    @Test
    void unitStep_blank_returnsErrorAndStaysOnStep() {
        driveToUnitStep("Água", "agua");

        TrackerChatResponse response = interaction.processInput("");

        assertFalse(response.completed());
        assertTrue(response.text().toLowerCase().contains("unidade"));
    }

    // -------------------------------------------------------------------------
    // Goal step validation
    // -------------------------------------------------------------------------

    @Test
    void goalStep_nonNumeric_returnsErrorAndStaysOnStep() {
        driveToGoalStep("Água", "agua", "ml");

        TrackerChatResponse response = interaction.processInput("muita");

        assertFalse(response.completed());
        assertNull(response.tracker());
        assertTrue(response.text().toLowerCase().contains("meta inválida"));

        // Valid retry completes
        TrackerChatResponse retry = interaction.processInput("2000");
        assertTrue(retry.completed());
    }

    @Test
    void goalStep_zeroOrNegative_returnsError() {
        driveToGoalStep("Água", "agua", "ml");

        assertFalse(interaction.processInput("0").completed());
        assertFalse(interaction.processInput("-5").completed());
    }

    // -------------------------------------------------------------------------
    // Cancel message
    // -------------------------------------------------------------------------

    @Test
    void cancelMessage_returnsExpectedText() {
        assertEquals("Criação de acompanhamento cancelada.", interaction.cancelMessage());
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private void driveToKeywordStep(String name) {
        interaction.processInput("@ctracker");
        interaction.processInput(name);
    }

    private void driveToUnitStep(String name, String keyword) {
        driveToKeywordStep(name);
        interaction.processInput(keyword);
    }

    private void driveToGoalStep(String name, String keyword, String unit) {
        driveToUnitStep(name, keyword);
        interaction.processInput(unit);
    }
}

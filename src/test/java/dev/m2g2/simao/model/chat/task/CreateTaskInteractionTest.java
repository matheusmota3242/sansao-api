package dev.m2g2.simao.model.chat.task;

import dev.m2g2.simao.dto.chat.TaskChatResponse;
import dev.m2g2.simao.model.task.TaskScheduler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CreateTaskInteractionTest {

    private CreateTaskInteraction interaction;

    @BeforeEach
    void setUp() {
        interaction = new CreateTaskInteraction();
    }

    // -------------------------------------------------------------------------
    // Initial trigger
    // -------------------------------------------------------------------------

    @Test
    void initialTrigger_returnsDescriptionStepPrompt() {
        TaskChatResponse response = interaction.processInput("@ctask");

        assertFalse(response.completed());
        assertNull(response.task());
        assertTrue(response.text().contains("Descrição da tarefa"));
    }

    // -------------------------------------------------------------------------
    // Description step
    // -------------------------------------------------------------------------

    @Test
    void descriptionStep_capturesValueAndAdvancesToDatetimeStep() {
        interaction.processInput("@ctask");

        TaskChatResponse response = interaction.processInput("Buy groceries");

        assertFalse(response.completed());
        assertNull(response.task());
        assertTrue(response.text().contains("Data e hora"));
    }

    // -------------------------------------------------------------------------
    // Datetime step — valid inputs
    // -------------------------------------------------------------------------

    @Test
    void datetimeStep_withFormattedDate_advancesToPeriodicityStep() {
        driveToDatetimeStep("Buy groceries");

        TaskChatResponse response = interaction.processInput("25/12/2030 14:00");

        assertFalse(response.completed());
        assertNull(response.task());
        assertTrue(response.text().contains("Periodicidade"));
    }

    @Test
    void datetimeStep_withAmanha_setsScheduledAtToTomorrowAtMidnight() {
        driveToDatetimeStep("Buy groceries");
        interaction.processInput("amanha");

        TaskChatResponse response = interaction.processInput("skip");

        assertTrue(response.completed());
        assertNotNull(response.task());
        LocalDateTime expectedDay = LocalDateTime.now().plusDays(1);
        assertEquals(expectedDay.getDayOfMonth(), response.task().getScheduledAt().getDayOfMonth());
        assertEquals(0, response.task().getScheduledAt().getHour());
        assertEquals(0, response.task().getScheduledAt().getMinute());
    }

    @Test
    void datetimeStep_withTomorrow_setsScheduledAtToTomorrow() {
        driveToDatetimeStep("Buy groceries");
        interaction.processInput("tomorrow");

        TaskChatResponse response = interaction.processInput("skip");

        assertTrue(response.completed());
        LocalDateTime expectedDay = LocalDateTime.now().plusDays(1);
        assertEquals(expectedDay.getDayOfMonth(), response.task().getScheduledAt().getDayOfMonth());
    }

    // -------------------------------------------------------------------------
    // Datetime step — invalid input (BUG: currently swallowed silently)
    // The interaction catches the parse exception and falls back to now(),
    // advancing the step instead of rejecting the input. These tests document
    // the current behaviour and should be updated once the bug is fixed.
    // -------------------------------------------------------------------------

    @Test
    void datetimeStep_withGibberish_currentlyFallsBackToNowAndAdvances() {
        driveToDatetimeStep("Buy groceries");

        // Bug: invalid input is silently accepted and scheduledAt becomes ~now()
        TaskChatResponse response = interaction.processInput("not-a-date");

        // Document current (broken) behaviour — step advances instead of erroring
        assertFalse(response.completed());
        assertTrue(response.text().contains("Periodicidade"));
    }

    @Test
    void datetimeStep_withWrongFormat_currentlyFallsBackToNowAndAdvances() {
        driveToDatetimeStep("Buy groceries");

        // Bug: correct date but wrong format (should be dd/MM/yyyy HH:mm)
        TaskChatResponse response = interaction.processInput("2030-12-25 14:00");

        assertFalse(response.completed());
        assertTrue(response.text().contains("Periodicidade"));
    }

    // -------------------------------------------------------------------------
    // Periodicity step — valid inputs
    // -------------------------------------------------------------------------

    @Test
    void periodicityStep_skip_completesTaskWithNoPeriodicity() {
        driveToPeriodicityStep("Buy groceries", "25/12/2030 14:00");

        TaskChatResponse response = interaction.processInput("skip");

        assertTrue(response.completed());
        assertNotNull(response.task());
        assertEquals("Buy groceries", response.task().getDescription());
        assertNull(response.task().getPeriodicity());
    }

    @Test
    void periodicityStep_daily_setsDailyPeriodicityAndCompletes() {
        driveToPeriodicityStep("Meeting", "01/06/2030 09:00");

        TaskChatResponse response = interaction.processInput("0");

        assertTrue(response.completed());
        assertNotNull(response.task());
        assertEquals(TaskScheduler.Periodicity.DAILY, response.task().getPeriodicity());
        assertEquals("Tarefa cadastrada com sucesso!", response.text());
    }

    @Test
    void periodicityStep_weeklyCustom_setsWeeklyCustomAndCompletes() {
        driveToPeriodicityStep("Gym", "01/06/2030 06:00");

        TaskChatResponse response = interaction.processInput("1 SEG,QUA,SEX");

        assertTrue(response.completed());
        assertNotNull(response.task());
        assertEquals(TaskScheduler.Periodicity.WEEKLY_CUSTOM, response.task().getPeriodicity());
        assertNotNull(response.task().getCustomSchedule());

        // Assert the actual parsed days, not just key existence
        String[] savedDays = (String[]) response.task().getCustomSchedule().get("daysOfWeek");
        List<String> dayList = Arrays.asList(savedDays);
        assertTrue(dayList.contains("SEG"));
        assertTrue(dayList.contains("QUA"));
        assertTrue(dayList.contains("SEX"));
        assertEquals(3, dayList.size());
    }

    @Test
    void periodicityStep_weeklyCustom_allSevenDays_completes() {
        driveToPeriodicityStep("Daily standup", "01/06/2030 09:00");

        TaskChatResponse response = interaction.processInput("1 SEG,TER,QUA,QUI,SEX,SAB,DOM");

        assertTrue(response.completed());
        assertEquals(TaskScheduler.Periodicity.WEEKLY_CUSTOM, response.task().getPeriodicity());
    }

    // -------------------------------------------------------------------------
    // Periodicity step — invalid inputs, state machine stays on same step
    // -------------------------------------------------------------------------

    @Test
    void periodicityStep_invalidDayAbbreviation_returnsErrorAndStaysOnStep() {
        driveToPeriodicityStep("Gym", "01/06/2030 06:00");

        TaskChatResponse firstResponse = interaction.processInput("1 MON,WED");

        assertFalse(firstResponse.completed());
        assertNull(firstResponse.task());
        assertTrue(firstResponse.text().toLowerCase().contains("inválido"));

        // State machine must stay on periodicity — a valid retry should succeed
        TaskChatResponse retryResponse = interaction.processInput("1 SEG,QUA");

        assertTrue(retryResponse.completed());
        assertEquals(TaskScheduler.Periodicity.WEEKLY_CUSTOM, retryResponse.task().getPeriodicity());
    }

    @Test
    void periodicityStep_tooManyDays_returnsErrorAndStaysOnStep() {
        driveToPeriodicityStep("Gym", "01/06/2030 06:00");

        TaskChatResponse firstResponse = interaction.processInput("1 SEG,TER,QUA,QUI,SEX,SAB,DOM,SEG");

        assertFalse(firstResponse.completed());
        assertNull(firstResponse.task());

        // Retry with valid input should complete
        TaskChatResponse retryResponse = interaction.processInput("0");

        assertTrue(retryResponse.completed());
        assertEquals(TaskScheduler.Periodicity.DAILY, retryResponse.task().getPeriodicity());
    }

    @Test
    void periodicityStep_weeklyCustom_missingDaysList_doesNotCrash() {
        driveToPeriodicityStep("Gym", "01/06/2030 06:00");

        // "1" without day list — should not throw ArrayIndexOutOfBoundsException
        assertDoesNotThrow(() -> interaction.processInput("1"));
    }

    // -------------------------------------------------------------------------
    // Cancel message
    // -------------------------------------------------------------------------

    @Test
    void cancelMessage_returnsExpectedText() {
        assertEquals("Criação de tarefa cancelada!", interaction.cancelMessage());
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private void driveToDatetimeStep(String description) {
        interaction.processInput("@ctask");
        interaction.processInput(description);
    }

    private void driveToPeriodicityStep(String description, String datetime) {
        driveToDatetimeStep(description);
        interaction.processInput(datetime);
    }
}
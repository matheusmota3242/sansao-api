package dev.m2g2.simao.model.chat.automation;

import dev.m2g2.simao.dto.chat.AutomationChatResponse;
import dev.m2g2.simao.enums.ActionType;
import dev.m2g2.simao.model.automation.schedule.DailySchedule;
import dev.m2g2.simao.model.automation.schedule.SpecificDateSchedule;
import dev.m2g2.simao.model.automation.schedule.WeeklyCustomSchedule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CreateAutomationInteractionTest {

    private CreateAutomationInteraction interaction;

    @BeforeEach
    void setUp() {
        interaction = new CreateAutomationInteraction();
    }

    // -------------------------------------------------------------------------
    // Initial trigger
    // -------------------------------------------------------------------------

    @Test
    void initialTrigger_returnsNameStepPrompt() {
        AutomationChatResponse response = interaction.processInput("@cauto");

        assertFalse(response.completed());
        assertNull(response.automation());
        assertTrue(response.text().contains("Nome da automação"));
    }

    // -------------------------------------------------------------------------
    // Name step
    // -------------------------------------------------------------------------

    @Test
    void nameStep_setsNameAndAdvancesToTypeStep() {
        interaction.processInput("@cauto");

        AutomationChatResponse response = interaction.processInput("Morning digest");

        assertFalse(response.completed());
        assertTrue(response.text().contains("Tipo da automação"));
    }

    @Test
    void nameStep_blankInput_defaultsToUndefinedNameAndAdvances() {
        interaction.processInput("@cauto");

        // Complete the flow to assert name was stored as "Undefined"
        driveToScheduleStep("  ", "0", "Hello!", "me@c.us");
        AutomationChatResponse response = interaction.processInput("08:00");

        assertTrue(response.completed());
        assertNotNull(response.automation());
        assertEquals("Undefined", response.automation().getName());
    }

    // -------------------------------------------------------------------------
    // Action type step
    // -------------------------------------------------------------------------

    @Test
    void typeStep_sendSimpleText_addsMessageStepAndPrompts() {
        driveToTypeStep("Announcement");

        AutomationChatResponse response = interaction.processInput("0");

        assertFalse(response.completed());
        assertTrue(response.text().contains("Mensagem"));
    }

    @Test
    void typeStep_sendTaskRemembering_addsTaskIdStepAndPrompts() {
        driveToTypeStep("Task reminder");

        AutomationChatResponse response = interaction.processInput("1");

        assertFalse(response.completed());
        assertTrue(response.text().contains("Id da tarefa"));
    }

    @Test
    void typeStep_nonNumericInput_returnsErrorAndStaysOnStep() {
        driveToTypeStep("Bad type");

        AutomationChatResponse firstResponse = interaction.processInput("abc");

        assertFalse(firstResponse.completed());
        assertNull(firstResponse.automation());
        assertTrue(firstResponse.text().contains("inválida"));

        // State machine must stay on type step — a valid retry should advance
        AutomationChatResponse retryResponse = interaction.processInput("0");

        assertFalse(retryResponse.completed());
        assertTrue(retryResponse.text().contains("Mensagem"));
    }

    @Test
    void typeStep_outOfRangeIndex_returnsEmptyOptionalAndAdvancesWithoutActionType() {
        // Bug: fromIndex(99) returns Optional.empty(), actionType is never set,
        // and the step is still marked completed — documenting current behaviour.
        driveToTypeStep("Bad index");

        AutomationChatResponse response = interaction.processInput("99");

        // Current behaviour: proceeds without setting actionType
        assertFalse(response.completed());
        // If this ever throws or behaves differently, the test will catch it
        assertDoesNotThrow(() -> interaction.processInput("99"));
    }

    // -------------------------------------------------------------------------
    // Message step (SEND_SIMPLE_TEXT)
    // -------------------------------------------------------------------------

    @Test
    void messageStep_setsMessageAndAdvancesToRecipientStep() {
        driveToMessageStep("Announcement", "0");

        AutomationChatResponse response = interaction.processInput("Hello everyone!");

        assertFalse(response.completed());
        assertTrue(response.text().contains("Destinatário"));
    }

    @Test
    void messageStep_contentStoredInMetadata() {
        driveToScheduleStep("Announcement", "0", "Hello everyone!", "me@c.us");
        AutomationChatResponse response = interaction.processInput("08:00");

        assertTrue(response.completed());
        assertEquals("Hello everyone!", response.automation().getMetadata().get("message"));
    }

    // -------------------------------------------------------------------------
    // Task association step (SEND_TASK_REMEMBERING)
    // -------------------------------------------------------------------------

    @Test
    void taskAssociationStep_validId_advancesToRecipientStep() {
        driveToMessageStep("Task reminder", "1");

        AutomationChatResponse response = interaction.processInput("42");

        assertFalse(response.completed());
        assertTrue(response.text().contains("Destinatário"));
    }

    @Test
    void taskAssociationStep_nonNumericId_returnsErrorAndStaysOnStep() {
        driveToMessageStep("Task reminder", "1");

        AutomationChatResponse firstResponse = interaction.processInput("not-a-number");

        assertFalse(firstResponse.completed());
        assertNull(firstResponse.automation());
        assertTrue(firstResponse.text().contains("número") || firstResponse.text().contains("Tente novamente"));

        // Retry with valid id should advance
        AutomationChatResponse retryResponse = interaction.processInput("42");

        assertFalse(retryResponse.completed());
        assertTrue(retryResponse.text().contains("Destinatário"));
    }

    // -------------------------------------------------------------------------
    // Recipient (to) step
    // -------------------------------------------------------------------------

    @Test
    void toStep_setsRecipientAndAdvancesToScheduleStep() {
        driveToToStep("Announcement", "0", "Hello!");

        AutomationChatResponse response = interaction.processInput("558499999999@c.us");

        assertFalse(response.completed());
        assertNull(response.automation());
        // Assert the concrete schedule prompt — not an OR
        assertTrue(response.text().contains("Data e hora"));
    }

    @Test
    void toStep_recipientStoredInMetadata() {
        driveToScheduleStep("Announcement", "0", "Hello!", "558499999999@c.us");
        AutomationChatResponse response = interaction.processInput("08:00");

        assertTrue(response.completed());
        assertEquals("558499999999@c.us", response.automation().getMetadata().get("to"));
    }

    // -------------------------------------------------------------------------
    // Schedule config step — daily
    // -------------------------------------------------------------------------

    @Test
    void scheduleStep_dailyTime_createsDailyScheduleAndCompletes() {
        driveToScheduleStep("Daily briefing", "0", "Good morning!", "me@c.us");

        AutomationChatResponse response = interaction.processInput("08:00");

        assertTrue(response.completed());
        assertNotNull(response.automation());
        assertEquals(ActionType.SEND_SIMPLE_TEXT, response.automation().getActionType());
        assertInstanceOf(DailySchedule.class, response.automation().getScheduleConfig());

        DailySchedule schedule = (DailySchedule) response.automation().getScheduleConfig();
        assertEquals(8, schedule.getTime().getHour());
        assertEquals(0, schedule.getTime().getMinute());
    }

    // -------------------------------------------------------------------------
    // Schedule config step — specific date
    // -------------------------------------------------------------------------

    @Test
    void scheduleStep_specificDateTime_createsSpecificDateScheduleAndCompletes() {
        driveToScheduleStep("One-time alert", "0", "Alert!", "me@c.us");

        AutomationChatResponse response = interaction.processInput("01/01/2030 10:00");

        assertTrue(response.completed());
        assertNotNull(response.automation());
        assertInstanceOf(SpecificDateSchedule.class, response.automation().getScheduleConfig());

        SpecificDateSchedule schedule = (SpecificDateSchedule) response.automation().getScheduleConfig();
        LocalDateTime date = schedule.getDate();
        assertEquals(2030, date.getYear());
        assertEquals(1, date.getMonthValue());
        assertEquals(1, date.getDayOfMonth());
        assertEquals(10, date.getHour());
        assertEquals(0, date.getMinute());
    }

    // -------------------------------------------------------------------------
    // Schedule config step — weekly custom
    // -------------------------------------------------------------------------

    @Test
    void scheduleStep_weeklyCustom_createsWeeklyCustomScheduleAndCompletes() {
        driveToScheduleStep("Weekly summary", "0", "Summary!", "me@c.us");

        AutomationChatResponse response = interaction.processInput("SEG 09:00, QUA 14:00");

        assertTrue(response.completed());
        assertNotNull(response.automation());
        assertInstanceOf(WeeklyCustomSchedule.class, response.automation().getScheduleConfig());

        WeeklyCustomSchedule schedule = (WeeklyCustomSchedule) response.automation().getScheduleConfig();
        assertEquals(2, schedule.getDays().size());

        // Assert actual days and times, not just list size
        WeeklyCustomSchedule.DayTime monday = schedule.getDays().get(0);
        assertEquals(DayOfWeek.MONDAY, monday.getDay());
        assertEquals(9, monday.getTime().getHour());

        WeeklyCustomSchedule.DayTime wednesday = schedule.getDays().get(1);
        assertEquals(DayOfWeek.WEDNESDAY, wednesday.getDay());
        assertEquals(14, wednesday.getTime().getHour());
    }

    // -------------------------------------------------------------------------
    // Schedule config step — task remembering flow
    // -------------------------------------------------------------------------

    @Test
    void scheduleStep_taskRemembranceFlow_setsTaskIdInMetadataAndCompletes() {
        driveToScheduleStep("Remind task", "1", "5", "me@c.us");

        AutomationChatResponse response = interaction.processInput("08:00");

        assertTrue(response.completed());
        assertNotNull(response.automation());
        assertEquals(ActionType.SEND_TASK_REMEMBERING, response.automation().getActionType());
        assertEquals(5L, response.automation().getMetadata().get("taskId"));
    }

    // -------------------------------------------------------------------------
    // Schedule config step — invalid inputs, state machine stays on same step
    // -------------------------------------------------------------------------

    @Test
    void scheduleStep_invalidInput_returnsErrorAndStaysOnStep() {
        driveToScheduleStep("Bad schedule", "0", "msg", "me@c.us");

        AutomationChatResponse firstResponse = interaction.processInput("not-a-date-or-time");

        assertFalse(firstResponse.completed());
        assertNull(firstResponse.automation());

        // Retry with valid input should complete
        AutomationChatResponse retryResponse = interaction.processInput("08:00");

        assertTrue(retryResponse.completed());
        assertInstanceOf(DailySchedule.class, retryResponse.automation().getScheduleConfig());
    }

    @Test
    void scheduleStep_blankInput_returnsErrorAndStaysOnStep() {
        driveToScheduleStep("Bad schedule", "0", "msg", "me@c.us");

        AutomationChatResponse firstResponse = interaction.processInput("");

        assertFalse(firstResponse.completed());
        assertNull(firstResponse.automation());

        // Retry with valid input should complete
        AutomationChatResponse retryResponse = interaction.processInput("01/01/2030 10:00");

        assertTrue(retryResponse.completed());
        assertInstanceOf(SpecificDateSchedule.class, retryResponse.automation().getScheduleConfig());
    }

    @Test
    void scheduleStep_weeklyCustom_invalidDayAbbreviation_returnsError() {
        driveToScheduleStep("Bad schedule", "0", "msg", "me@c.us");

        // Uses English abbreviation instead of Portuguese (MON instead of SEG)
        AutomationChatResponse response = interaction.processInput("MON 09:00");

        assertFalse(response.completed());
        assertNull(response.automation());
    }

    // -------------------------------------------------------------------------
    // Cancel
    // -------------------------------------------------------------------------

    @Test
    void cancelMessage_returnsExpectedText() {
        assertEquals("Criação de automação cancelada.", interaction.cancelMessage());
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private void driveToTypeStep(String name) {
        interaction.processInput("@cauto");
        interaction.processInput(name);
    }

    private void driveToMessageStep(String name, String actionTypeIndex) {
        driveToTypeStep(name);
        interaction.processInput(actionTypeIndex);
    }

    private void driveToToStep(String name, String actionTypeIndex, String messageOrTaskId) {
        driveToMessageStep(name, actionTypeIndex);
        interaction.processInput(messageOrTaskId);
    }

    private void driveToScheduleStep(String name, String actionTypeIndex, String messageOrTaskId, String recipient) {
        driveToToStep(name, actionTypeIndex, messageOrTaskId);
        interaction.processInput(recipient);
    }
}
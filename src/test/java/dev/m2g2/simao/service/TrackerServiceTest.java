package dev.m2g2.simao.service;

import dev.m2g2.simao.dto.TrackerDTO;
import dev.m2g2.simao.model.tracker.Tracker;
import dev.m2g2.simao.model.tracker.TrackerEntry;
import dev.m2g2.simao.repository.TrackerEntryRepository;
import dev.m2g2.simao.repository.TrackerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrackerServiceTest {

    @Mock
    private TrackerRepository repository;
    @Mock
    private TrackerEntryRepository entryRepository;
    @Mock
    private ChatRecordService chatRecordService;

    @InjectMocks
    private TrackerService service;

    private Tracker tracker(String keyword, String unit, String goal) {
        Tracker t = new Tracker();
        t.setId(1L);
        t.setName("Água");
        t.setKeyword(keyword);
        t.setUnit(unit);
        t.setDailyGoal(new BigDecimal(goal));
        return t;
    }

    // -------------------------------------------------------------------------
    // logIf — routing / no-match
    // -------------------------------------------------------------------------

    @Test
    void logIf_nonCommandMessage_returnsNull() {
        assertNull(service.logIf("bom dia"));
        verifyNoInteractions(repository, entryRepository);
    }

    @Test
    void logIf_unknownKeyword_returnsNull() {
        when(repository.findByKeywordAndActiveTrue("desconhecido")).thenReturn(Optional.empty());

        assertNull(service.logIf("@desconhecido 250"));
        verify(entryRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // logIf — progress query (no amount)
    // -------------------------------------------------------------------------

    @Test
    void logIf_keywordWithoutAmount_returnsProgressWithoutSaving() {
        when(repository.findByKeywordAndActiveTrue("agua")).thenReturn(Optional.of(tracker("agua", "ml", "2000")));
        when(entryRepository.sumAmountForPeriod(anyLong(), any(), any())).thenReturn(new BigDecimal("800"));

        String reply = service.logIf("@agua");

        assertTrue(reply.contains("800/2000 ml"));
        assertTrue(reply.contains("faltam 1200 ml"));
        verify(entryRepository, never()).save(any());
    }

    @Test
    void logIf_keywordLookupIsLowercased() {
        when(repository.findByKeywordAndActiveTrue("agua")).thenReturn(Optional.of(tracker("agua", "ml", "2000")));
        when(entryRepository.sumAmountForPeriod(anyLong(), any(), any())).thenReturn(BigDecimal.ZERO);

        assertNotNull(service.logIf("@AGUA"));
        verify(repository).findByKeywordAndActiveTrue("agua");
    }

    // -------------------------------------------------------------------------
    // logIf — logging an amount
    // -------------------------------------------------------------------------

    @Test
    void logIf_validAmount_savesEntryAndReturnsProgress() {
        when(repository.findByKeywordAndActiveTrue("agua")).thenReturn(Optional.of(tracker("agua", "ml", "2000")));
        when(entryRepository.sumAmountForPeriod(anyLong(), any(), any())).thenReturn(new BigDecimal("250"));

        String reply = service.logIf("@agua 250");

        ArgumentCaptor<TrackerEntry> captor = ArgumentCaptor.forClass(TrackerEntry.class);
        verify(entryRepository).save(captor.capture());
        TrackerEntry saved = captor.getValue();
        assertEquals(0, new BigDecimal("250").compareTo(saved.getAmount()));
        assertEquals("agua", saved.getTracker().getKeyword());
        assertNotNull(saved.getRecordedAt());
        assertTrue(saved.isActive());
        assertTrue(reply.contains("250/2000 ml"));
    }

    @Test
    void logIf_commaDecimalAmount_isParsed() {
        when(repository.findByKeywordAndActiveTrue("prot")).thenReturn(Optional.of(tracker("prot", "g", "150")));
        when(entryRepository.sumAmountForPeriod(anyLong(), any(), any())).thenReturn(new BigDecimal("30.5"));

        service.logIf("@prot 30,5");

        ArgumentCaptor<TrackerEntry> captor = ArgumentCaptor.forClass(TrackerEntry.class);
        verify(entryRepository).save(captor.capture());
        assertEquals(0, new BigDecimal("30.5").compareTo(captor.getValue().getAmount()));
    }

    @Test
    void logIf_goalReached_showsGoalMetMessage() {
        when(repository.findByKeywordAndActiveTrue("agua")).thenReturn(Optional.of(tracker("agua", "ml", "2000")));
        when(entryRepository.sumAmountForPeriod(anyLong(), any(), any())).thenReturn(new BigDecimal("2000"));

        String reply = service.logIf("@agua 500");

        assertTrue(reply.contains("meta batida"));
    }

    @Test
    void logIf_invalidAmount_returnsErrorAndDoesNotSave() {
        when(repository.findByKeywordAndActiveTrue("agua")).thenReturn(Optional.of(tracker("agua", "ml", "2000")));

        String reply = service.logIf("@agua muita");

        assertTrue(reply.toLowerCase().contains("quantidade inválida"));
        verify(entryRepository, never()).save(any());
    }

    @Test
    void logIf_negativeAmount_returnsErrorAndDoesNotSave() {
        when(repository.findByKeywordAndActiveTrue("agua")).thenReturn(Optional.of(tracker("agua", "ml", "2000")));

        assertTrue(service.logIf("@agua -50").toLowerCase().contains("inválida"));
        verify(entryRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // createFromChat — keyword uniqueness
    // -------------------------------------------------------------------------

    @Test
    void createFromChat_newKeyword_savesAndReturnsNull() {
        when(repository.existsByKeyword("agua")).thenReturn(false);
        TrackerDTO dto = dto("Água", "agua", "ml", "2000");

        String outcome = service.createFromChat(dto);

        assertNull(outcome);
        ArgumentCaptor<Tracker> captor = ArgumentCaptor.forClass(Tracker.class);
        verify(repository).save(captor.capture());
        Tracker saved = captor.getValue();
        assertEquals("agua", saved.getKeyword());
        assertEquals("ml", saved.getUnit());
        assertTrue(saved.isActive());
        assertNotNull(saved.getCreatedAt());
    }

    @Test
    void createFromChat_duplicateKeyword_returnsErrorAndDoesNotSave() {
        when(repository.existsByKeyword("agua")).thenReturn(true);

        String outcome = service.createFromChat(dto("Água", "agua", "ml", "2000"));

        assertNotNull(outcome);
        assertTrue(outcome.toLowerCase().contains("já existe"));
        verify(repository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // listIf / deleteIf — non-matching messages
    // -------------------------------------------------------------------------

    @Test
    void listIf_wrongCommand_returnsNull() {
        assertNull(service.listIf("@ltask"));
    }

    @Test
    void deleteIf_missingId_returnsUsage() {
        assertTrue(service.deleteIf("@dtracker").toLowerCase().contains("uso"));
    }

    @Test
    void deleteIf_unknownId_returnsNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertTrue(service.deleteIf("@dtracker 99").toLowerCase().contains("não encontrado"));
        verify(repository, never()).save(any());
    }

    @Test
    void deleteIf_existingId_softDeletes() {
        Tracker t = tracker("agua", "ml", "2000");
        when(repository.findById(1L)).thenReturn(Optional.of(t));

        String reply = service.deleteIf("@dtracker 1");

        assertFalse(t.isActive());
        verify(repository).save(t);
        assertTrue(reply.contains("removido"));
    }

    private TrackerDTO dto(String name, String keyword, String unit, String goal) {
        TrackerDTO dto = new TrackerDTO();
        dto.setName(name);
        dto.setKeyword(keyword);
        dto.setUnit(unit);
        dto.setDailyGoal(new BigDecimal(goal));
        return dto;
    }
}

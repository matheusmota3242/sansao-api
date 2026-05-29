package dev.m2g2.simao.repository;

import dev.m2g2.simao.model.Note;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoteRepository extends JpaRepository<Note, Long> {
    List<Note> findAllByActiveTrueOrderByCreatedAtDesc();
}

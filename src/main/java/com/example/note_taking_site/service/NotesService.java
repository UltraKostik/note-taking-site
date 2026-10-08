package com.example.note_taking_site.service;

import com.example.note_taking_site.dto.NoteRequest;
import com.example.note_taking_site.exception.NoteNotFoundException;
import com.example.note_taking_site.model.Notes;
import com.example.note_taking_site.model.User;
import com.example.note_taking_site.repository.NotesRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotesService {

    private static final Logger log = LoggerFactory.getLogger(NotesService.class);

    private final NotesRepository notesRepository;

    public NotesService(NotesRepository notesRepository) {
        this.notesRepository = notesRepository;
    }

    public Notes createNote(NoteRequest request, User author) {
        Notes note = new Notes();
        note.setTitle(request.getTitle());
        note.setContent(request.getContent());
        note.setAuthor(author);
        note.setStatus(Notes.NoteStatus.ACTIVE);
        log.info("Creating note '{}' for user {}", request.getTitle(), author.getEmail());
        return notesRepository.save(note);
    }

    public List<Notes> getAllNotes() {
        return notesRepository.findAll();
    }

    public List<Notes> getActiveNotes(User author) {
        return notesRepository.findAllByAuthorAndStatusOrderByUpdatedAtDesc(author, Notes.NoteStatus.ACTIVE);
    }

    public List<Notes> getDeletedNotes(User author) {
        return notesRepository.findAllByAuthorAndStatusOrderByUpdatedAtDesc(author, Notes.NoteStatus.DELETED);
    }

    public Notes getNoteById(Long id, User author) {
        return notesRepository.findByIdAndAuthor(id, author)
                .orElseThrow(() -> new NoteNotFoundException(id));
    }

    public List<Notes> getAllByStatus(Notes.NoteStatus status) {
        return notesRepository.findAllByStatusOrderByUpdatedAtDesc(status);
    }

    public void updateNote(Long id, NoteRequest request, User author) {
        Notes note = notesRepository.findByIdAndAuthor(id, author)
                .orElseThrow(() -> new NoteNotFoundException(id));
        note.setTitle(request.getTitle());
        note.setContent(request.getContent());
        notesRepository.save(note);
        log.info("Updating note id={} by user {}", id, author.getEmail());
    }

    public void moveToTrash(Long id, User author) {
        Notes note = notesRepository.findByIdAndAuthor(id, author)
                .orElseThrow(() -> new NoteNotFoundException(id));
        note.setStatus(Notes.NoteStatus.DELETED);
        notesRepository.save(note);
        log.info("Moving note id={} to trash by user {}", id, author.getEmail());
    }

    public void restoreFromTrash(Long id, User author) {
        Notes note = notesRepository.findByIdAndAuthor(id, author)
                .orElseThrow(() -> new NoteNotFoundException(id));
        note.setStatus(Notes.NoteStatus.ACTIVE);
        notesRepository.save(note);
        log.info("Restoring note id={} from trash by user {}", id, author.getEmail());
    }

    public void deletePermanently(Long id, User author) {
        Notes note = notesRepository.findByIdAndAuthor(id, author)
                .orElseThrow(() -> new NoteNotFoundException(id));
        notesRepository.delete(note);
        log.info("Permanently deleting note id={} by user {}", id, author.getEmail());
    }

    public void deletePermanentlyAdmin(Long id) {
        notesRepository.deleteById(id);
        log.info("Permanently deleting note id={} by admin", id);
    }

    public void clearTrash(User author) {
        List<Notes> deleted = getDeletedNotes(author);
        notesRepository.deleteAll(deleted);
        log.info("Cleared trash for user {}, deleted {} notes", author.getEmail(), deleted.size());
    }
}
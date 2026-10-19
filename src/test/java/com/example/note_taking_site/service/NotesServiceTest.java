package com.example.note_taking_site.service;

import com.example.note_taking_site.dto.NoteRequest;
import com.example.note_taking_site.model.Notes;
import com.example.note_taking_site.model.User;
import com.example.note_taking_site.repository.NotesRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotesServiceTest {

    @Mock
    private NotesRepository notesRepository;

    @InjectMocks
    private NotesService notesService;

    private User user(Long id, String email) {
        User u = new User();
        u.setId(id);
        u.setEmail(email);
        return u;
    }

    private Notes note(Long id, String title, Notes.NoteStatus status, User author) {
        Notes n = new Notes();
        n.setId(id);
        n.setTitle(title);
        n.setContent("содержимое " + id);
        n.setStatus(status);
        n.setAuthor(author);
        return n;
    }

    private NoteRequest request(String title, String content) {
        NoteRequest r = new NoteRequest();
        r.setTitle(title);
        r.setContent(content);
        return r;
    }

    @Test
    void createNote_setsActiveStatusAndAuthor() {
        User author = user(1L, "author@example.com");
        when(notesRepository.save(any(Notes.class))).thenAnswer(inv -> inv.getArgument(0));

        Notes result = notesService.createNote(request("Моя заметка", "текст"), author);

        assertThat(result.getTitle()).isEqualTo("Моя заметка");
        assertThat(result.getContent()).isEqualTo("текст");
        assertThat(result.getStatus()).isEqualTo(Notes.NoteStatus.ACTIVE);
        assertThat(result.getAuthor()).isSameAs(author);
        verify(notesRepository, times(1)).save(result);
    }

    @Test
    void getActiveNotes_returnsOnlyActiveForAuthor() {
        User author = user(1L, "author@example.com");
        List<Notes> active = List.of(
                note(1L, "A", Notes.NoteStatus.ACTIVE, author),
                note(2L, "B", Notes.NoteStatus.ACTIVE, author));
        when(notesRepository.findAllByAuthorAndStatusOrderByUpdatedAtDesc(author, Notes.NoteStatus.ACTIVE))
                .thenReturn(active);

        List<Notes> result = notesService.getActiveNotes(author);

        assertThat(result).isEqualTo(active);
        verify(notesRepository).findAllByAuthorAndStatusOrderByUpdatedAtDesc(author, Notes.NoteStatus.ACTIVE);
    }

    @Test
    void getDeletedNotes_returnsOnlyDeletedForAuthor() {
        User author = user(1L, "author@example.com");
        List<Notes> deleted = List.of(note(3L, "C", Notes.NoteStatus.DELETED, author));
        when(notesRepository.findAllByAuthorAndStatusOrderByUpdatedAtDesc(author, Notes.NoteStatus.DELETED))
                .thenReturn(deleted);

        List<Notes> result = notesService.getDeletedNotes(author);

        assertThat(result).isEqualTo(deleted);
        verify(notesRepository).findAllByAuthorAndStatusOrderByUpdatedAtDesc(author, Notes.NoteStatus.DELETED);
    }

    @Test
    void updateNote_changesTitleAndContent() {
        User author = user(1L, "author@example.com");
        Notes existing = note(5L, "старое", Notes.NoteStatus.ACTIVE, author);
        when(notesRepository.findByIdAndAuthor(5L, author)).thenReturn(Optional.of(existing));

        notesService.updateNote(5L, request("новое", "новый текст"), author);

        assertThat(existing.getTitle()).isEqualTo("новое");
        assertThat(existing.getContent()).isEqualTo("новый текст");
        verify(notesRepository).save(existing);
    }

    @Test
    void updateNote_missingOrForeignNote_throws() {
        User author = user(1L, "author@example.com");
        when(notesRepository.findByIdAndAuthor(99L, author)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notesService.updateNote(99L, request("x", "y"), author))
                .isInstanceOf(RuntimeException.class);
        verify(notesRepository, never()).save(any(Notes.class));
    }

    @Test
    void moveToTrash_setsDeletedStatus() {
        User author = user(1L, "author@example.com");
        Notes existing = note(3L, "T", Notes.NoteStatus.ACTIVE, author);
        when(notesRepository.findByIdAndAuthor(3L, author)).thenReturn(Optional.of(existing));

        notesService.moveToTrash(3L, author);

        assertThat(existing.getStatus()).isEqualTo(Notes.NoteStatus.DELETED);
        verify(notesRepository).save(existing);
    }

    @Test
    void restoreFromTrash_setsActiveStatus() {
        User author = user(1L, "author@example.com");
        Notes existing = note(3L, "T", Notes.NoteStatus.DELETED, author);
        when(notesRepository.findByIdAndAuthor(3L, author)).thenReturn(Optional.of(existing));

        notesService.restoreFromTrash(3L, author);

        assertThat(existing.getStatus()).isEqualTo(Notes.NoteStatus.ACTIVE);
        verify(notesRepository).save(existing);
    }

    @Test
    void deletePermanently_removesNote() {
        User author = user(1L, "author@example.com");
        Notes existing = note(7L, "T", Notes.NoteStatus.DELETED, author);
        when(notesRepository.findByIdAndAuthor(7L, author)).thenReturn(Optional.of(existing));

        notesService.deletePermanently(7L, author);

        verify(notesRepository).delete(existing);
    }

    @Test
    void clearTrash_deletesAllDeletedNotesOfUser() {
        User author = user(1L, "author@example.com");
        List<Notes> deleted = List.of(
                note(1L, "A", Notes.NoteStatus.DELETED, author),
                note(2L, "B", Notes.NoteStatus.DELETED, author));
        when(notesRepository.findAllByAuthorAndStatusOrderByUpdatedAtDesc(author, Notes.NoteStatus.DELETED))
                .thenReturn(deleted);

        notesService.clearTrash(author);

        verify(notesRepository).deleteAll(deleted);
    }

    @Test
    void deletePermanentlyAdmin_removesById() {
        notesService.deletePermanentlyAdmin(42L);

        verify(notesRepository).deleteById(42L);
    }

    @Test
    void getNoteByIdAdmin_returnsNote() {
        User author = user(1L, "author@example.com");
        Notes existing = note(9L, "T", Notes.NoteStatus.ACTIVE, author);
        when(notesRepository.findById(9L)).thenReturn(Optional.of(existing));

        assertThat(notesService.getNoteByIdAdmin(9L)).isSameAs(existing);
    }
}

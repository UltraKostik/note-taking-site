package com.example.note_taking_site.repository;

import com.example.note_taking_site.model.Attachment;
import com.example.note_taking_site.model.Notes;
import com.example.note_taking_site.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class RepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotesRepository notesRepository;

    @Autowired
    private AttachmentRepository attachmentRepository;

    private User persistUser(String email) {
        User u = new User();
        u.setEmail(email);
        u.setPassword("encoded");
        u.setRole(User.Role.USER);
        return userRepository.save(u);
    }

    private Notes persistNote(User author, String title, Notes.NoteStatus status) {
        Notes n = new Notes();
        n.setTitle(title);
        n.setContent("content " + title);
        n.setAuthor(author);
        n.setStatus(status);
        return notesRepository.save(n);
    }

    private Attachment persistAttachment(Notes note, String fileName) {
        Attachment a = new Attachment();
        a.setFileName(fileName);
        a.setContentType("text/plain");
        a.setSize(3L);
        a.setData("abc".getBytes());
        a.setNote(note);
        return attachmentRepository.save(a);
    }

    @Test
    void findByEmail_returnsUser() {
        persistUser("find@example.com");

        Optional<User> found = userRepository.findByEmail("find@example.com");

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("find@example.com");
    }

    @Test
    void existsByEmail_trueAndFalse() {
        persistUser("exists@example.com");

        assertThat(userRepository.existsByEmail("exists@example.com")).isTrue();
        assertThat(userRepository.existsByEmail("nope@example.com")).isFalse();
    }

    @Test
    void findByIdAndAuthor_respectsOwnership() {
        User author = persistUser("owner@example.com");
        User other = persistUser("intruder@example.com");
        Notes note = persistNote(author, "mine", Notes.NoteStatus.ACTIVE);

        assertThat(notesRepository.findByIdAndAuthor(note.getId(), author)).isPresent();
        assertThat(notesRepository.findByIdAndAuthor(note.getId(), other)).isEmpty();
    }

    @Test
    void deleteAllByAuthor_deletesOnlyThatAuthorsNotes() {
        User author = persistUser("delete-me@example.com");
        User keeper = persistUser("keep-me@example.com");
        persistNote(author, "a1", Notes.NoteStatus.ACTIVE);
        persistNote(author, "a2", Notes.NoteStatus.DELETED);
        Notes kept = persistNote(keeper, "k1", Notes.NoteStatus.ACTIVE);

        notesRepository.deleteAllByAuthor(author);
        notesRepository.flush();

        assertThat(notesRepository.findAllByAuthorOrderByUpdatedAtDesc(author)).isEmpty();
        assertThat(notesRepository.findById(kept.getId())).isPresent();
    }

    @Test
    void findAllByNote_returnsAttachmentsOfNote() {
        User author = persistUser("files@example.com");
        Notes note = persistNote(author, "with files", Notes.NoteStatus.ACTIVE);
        Notes otherNote = persistNote(author, "other", Notes.NoteStatus.ACTIVE);
        persistAttachment(note, "one.txt");
        persistAttachment(note, "two.txt");
        persistAttachment(otherNote, "three.txt");

        List<Attachment> attachments = attachmentRepository.findAllByNote(note);

        assertThat(attachments).extracting(Attachment::getFileName)
                .containsExactlyInAnyOrder("one.txt", "two.txt");
    }

    @Test
    void findByIdAndNote_missing_returnsEmpty() {
        User author = persistUser("att@example.com");
        Notes note = persistNote(author, "n", Notes.NoteStatus.ACTIVE);
        Notes otherNote = persistNote(author, "o", Notes.NoteStatus.ACTIVE);
        Attachment attachment = persistAttachment(otherNote, "file.txt");

        assertThat(attachmentRepository.findByIdAndNote(attachment.getId(), note)).isEmpty();
    }
}

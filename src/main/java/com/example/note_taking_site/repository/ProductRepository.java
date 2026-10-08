package com.example.note_taking_site.repository;

import com.example.note_taking_site.model.Notes;
import com.example.note_taking_site.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Notes, Long> {

    List<Notes> findAllByAuthorAndStatusOrderByUpdatedAtDesc(User author, Notes.NoteStatus status);

    List<Notes> findAllByStatusOrderByUpdatedAtDesc(Notes.NoteStatus status);

    Optional<Notes> findByIdAndAuthor(Long id, User author);

    void deleteAllByAuthor(User author);
}
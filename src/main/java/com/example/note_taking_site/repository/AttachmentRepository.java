package com.example.note_taking_site.repository;

import com.example.note_taking_site.model.Attachment;
import com.example.note_taking_site.model.Notes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

    List<Attachment> findAllByNote(Notes note);

    Optional<Attachment> findByIdAndNote(Long id, Notes note);
}
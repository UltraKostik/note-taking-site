package com.example.note_taking_site.service;

import com.example.note_taking_site.model.Notes;
import com.example.note_taking_site.model.User;
import com.example.note_taking_site.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public void saveProduct(Notes product) {
        productRepository.save(product);
    }

    public List<Notes> getAllProducts() {
        return productRepository.findAll();
    }

    // --- для конкретного пользователя ---

    public List<Notes> getActiveNotes(User author) {
        return productRepository.findAllByAuthorAndStatusOrderByUpdatedAtDesc(author, Notes.NoteStatus.ACTIVE);
    }

    public List<Notes> getDraftNotes(User author) {
        return productRepository.findAllByAuthorAndStatusOrderByUpdatedAtDesc(author, Notes.NoteStatus.DRAFT);
    }

    public List<Notes> getDeletedNotes(User author) {
        return productRepository.findAllByAuthorAndStatusOrderByUpdatedAtDesc(author, Notes.NoteStatus.DELETED);
    }

    public void moveToTrash(Long id, User author) {
        Notes note = productRepository.findByIdAndAuthor(id, author)
                .orElseThrow(() -> new IllegalArgumentException("Note not found: " + id));
        note.setStatus(Notes.NoteStatus.DELETED);
        productRepository.save(note);
    }

    public void restoreFromTrash(Long id, User author) {
        Notes note = productRepository.findByIdAndAuthor(id, author)
                .orElseThrow(() -> new IllegalArgumentException("Note not found: " + id));
        note.setStatus(Notes.NoteStatus.ACTIVE);
        productRepository.save(note);
    }

    public void moveToDraft(Long id, User author) {
        Notes note = productRepository.findByIdAndAuthor(id, author)
                .orElseThrow(() -> new IllegalArgumentException("Note not found: " + id));
        note.setStatus(Notes.NoteStatus.DRAFT);
        productRepository.save(note);
    }

    // --- для админа ---

    public List<Notes> getAllByStatus(Notes.NoteStatus status) {
        return productRepository.findAllByStatusOrderByUpdatedAtDesc(status);
    }
}
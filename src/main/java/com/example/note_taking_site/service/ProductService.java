package com.example.note_taking_site.service;

import com.example.note_taking_site.dto.NoteRequest;
import com.example.note_taking_site.exception.NoteNotFoundException;
import com.example.note_taking_site.model.Notes;
import com.example.note_taking_site.model.User;
import com.example.note_taking_site.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }


    public Notes createNote(NoteRequest request, User author) {
        Notes note = new Notes();
        note.setTitle(request.getTitle());
        note.setContent(request.getContent());
        note.setAuthor(author);
        note.setStatus(Notes.NoteStatus.ACTIVE);
        log.info("Creating note '{}' for user {}", request.getTitle(), author.getEmail());
        return productRepository.save(note);
    }


    public List<Notes> getAllProducts() {
        return productRepository.findAll();
    }

    public List<Notes> getActiveNotes(User author) {
        return productRepository.findAllByAuthorAndStatusOrderByUpdatedAtDesc(author, Notes.NoteStatus.ACTIVE);
    }

    public List<Notes> getDeletedNotes(User author) {
        return productRepository.findAllByAuthorAndStatusOrderByUpdatedAtDesc(author, Notes.NoteStatus.DELETED);
    }

    public Notes getNoteById(Long id, User author) {
        return productRepository.findByIdAndAuthor(id, author)
                .orElseThrow(() -> new NoteNotFoundException(id));
    }

    public List<Notes> getAllByStatus(Notes.NoteStatus status) {
        return productRepository.findAllByStatusOrderByUpdatedAtDesc(status);
    }


    public void saveProduct(Notes product) {
        productRepository.save(product);
    }

    public void updateNote(Long id, NoteRequest request, User author) {
        Notes note = productRepository.findByIdAndAuthor(id, author)
                .orElseThrow(() -> new NoteNotFoundException(id));
        note.setTitle(request.getTitle());
        note.setContent(request.getContent());
        productRepository.save(note);
        log.info("Updating note id={} by user {}", id, author.getEmail());
    }

    public void moveToTrash(Long id, User author) {
        Notes note = productRepository.findByIdAndAuthor(id, author)
                .orElseThrow(() -> new NoteNotFoundException(id));
        note.setStatus(Notes.NoteStatus.DELETED);
        productRepository.save(note);
        log.info("Moving note id={} to trash by user {}", id, author.getEmail());
    }

    public void restoreFromTrash(Long id, User author) {
        Notes note = productRepository.findByIdAndAuthor(id, author)
                .orElseThrow(() -> new NoteNotFoundException(id));
        note.setStatus(Notes.NoteStatus.ACTIVE);
        productRepository.save(note);
        log.info("Restoring note id={} from trash by user {}", id, author.getEmail());
    }


    public void deletePermanently(Long id, User author) {
        Notes note = productRepository.findByIdAndAuthor(id, author)
                .orElseThrow(() -> new NoteNotFoundException(id));
        productRepository.delete(note);
        log.info("Permanently deleting note id={} by user {}", id, author.getEmail());
    }

    public void deletePermanentlyAdmin(Long id) {
        productRepository.deleteById(id);
        log.info("Permanently deleting note id={} by admin", id);
    }

    public void clearTrash(User author) {
        List<Notes> deleted = getDeletedNotes(author);
        productRepository.deleteAll(deleted);
        log.info("Cleared trash for user {}, deleted {} notes", author.getEmail(), deleted.size());
    }
}
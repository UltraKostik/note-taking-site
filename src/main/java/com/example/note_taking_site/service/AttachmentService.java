package com.example.note_taking_site.service;

import com.example.note_taking_site.exception.AttachmentNotFoundException;
import com.example.note_taking_site.model.Attachment;
import com.example.note_taking_site.model.Notes;
import com.example.note_taking_site.repository.AttachmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class AttachmentService {

    private static final Logger log = LoggerFactory.getLogger(AttachmentService.class);

    private final AttachmentRepository attachmentRepository;

    public AttachmentService(AttachmentRepository attachmentRepository) {
        this.attachmentRepository = attachmentRepository;
    }

    public Attachment save(MultipartFile file, Notes note) throws IOException {
        Attachment attachment = new Attachment();
        attachment.setFileName(file.getOriginalFilename() == null
                ? "file" : file.getOriginalFilename());
        attachment.setContentType(file.getContentType());
        attachment.setSize(file.getSize());
        attachment.setData(file.getBytes());
        attachment.setNote(note);
        log.info("Saving attachment '{}' ({} bytes) for note id={}",
                attachment.getFileName(), attachment.getSize(), note.getId());
        return attachmentRepository.save(attachment);
    }

    public List<Attachment> getAllByNote(Notes note) {
        return attachmentRepository.findAllByNote(note);
    }

    public Attachment getById(Long id, Notes note) {
        return attachmentRepository.findByIdAndNote(id, note)
                .orElseThrow(() -> new AttachmentNotFoundException(id));
    }

    public void delete(Long id, Notes note) {
        Attachment attachment = attachmentRepository.findByIdAndNote(id, note)
                .orElseThrow(() -> new AttachmentNotFoundException(id));
        attachmentRepository.delete(attachment);
        log.info("Deleting attachment id={} from note id={}", id, note.getId());
    }
}
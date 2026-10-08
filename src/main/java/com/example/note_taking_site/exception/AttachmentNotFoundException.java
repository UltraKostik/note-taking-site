package com.example.note_taking_site.exception;

public class AttachmentNotFoundException extends RuntimeException {

    public AttachmentNotFoundException(Long id) {
        super("Attachment not found: " + id);
    }
}
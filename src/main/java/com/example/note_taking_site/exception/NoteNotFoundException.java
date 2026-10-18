package com.example.note_taking_site.exception;

public class NoteNotFoundException extends RuntimeException {
    public NoteNotFoundException(Long id) {
        super("Note not found: " + id);
    }
}
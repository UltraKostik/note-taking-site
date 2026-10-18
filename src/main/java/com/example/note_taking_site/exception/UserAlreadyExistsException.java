package com.example.note_taking_site.exception;

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String email) {
        super("Email already in use: " + email);
    }
}
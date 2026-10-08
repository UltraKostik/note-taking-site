package com.example.note_taking_site.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(NoteNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNoteNotFound(NoteNotFoundException e, Model model) {
        log.warn("Note not found: {}", e.getMessage());
        model.addAttribute("message", "Заметка не найдена");
        return "error/404";
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleUserExists(UserAlreadyExistsException e, Model model) {
        log.warn("User already exists: {}", e.getMessage());
        model.addAttribute("message", "Пользователь с таким email уже существует");
        return "error/409";
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleBadRequest(IllegalArgumentException e, Model model) {
        log.warn("Bad request: {}", e.getMessage());
        model.addAttribute("message", e.getMessage());
        return "error/400";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGeneric(Exception e, Model model) {
        log.error("Unexpected error", e);
        model.addAttribute("message", "Внутренняя ошибка сервера");
        return "error/500";
    }


    @ExceptionHandler(AttachmentNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleAttachmentNotFound(AttachmentNotFoundException e, Model model) {
        log.warn("Attachment not found: {}", e.getMessage());
        model.addAttribute("message", "Файл не найден");
        return "error/404";
    }
}
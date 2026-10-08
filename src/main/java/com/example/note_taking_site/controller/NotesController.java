package com.example.note_taking_site.controller;

import com.example.note_taking_site.dto.NoteRequest;
import com.example.note_taking_site.model.Attachment;
import com.example.note_taking_site.model.Notes;
import com.example.note_taking_site.model.User;
import com.example.note_taking_site.repository.UserRepository;
import com.example.note_taking_site.service.AttachmentService;
import com.example.note_taking_site.service.NotesService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Controller
public class NotesController {

    private final NotesService notesService;
    private final UserRepository userRepository;
    private final AttachmentService attachmentService;

    public NotesController(NotesService notesService,
                           UserRepository userRepository,
                           AttachmentService attachmentService) {
        this.notesService = notesService;
        this.userRepository = userRepository;
        this.attachmentService = attachmentService;
    }

    private User currentUser(Authentication auth) {
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found in DB"));
    }

    @GetMapping("/notes")
    public String listActive(Authentication auth, Model model) {
        User user = currentUser(auth);
        model.addAttribute("notes", notesService.getActiveNotes(user));
        return "notes-list";
    }

    @GetMapping("/trash")
    public String listTrash(Authentication auth, Model model) {
        User user = currentUser(auth);
        model.addAttribute("deletedNotes", notesService.getDeletedNotes(user));
        return "trash";
    }

    @GetMapping("/notes/add")
    public String formAdd(Model model) {
        model.addAttribute("noteRequest", new NoteRequest());
        return "note-form";
    }

    @PostMapping("/notes/add")
    public String add(@Valid @ModelAttribute("noteRequest") NoteRequest request,
                      BindingResult bindingResult,
                      Authentication auth) {
        if (bindingResult.hasErrors()) {
            return "note-form";
        }
        notesService.createNote(request, currentUser(auth));
        return "redirect:/notes";
    }

    @GetMapping("/notes/{id}")
    public String view(@PathVariable Long id, Authentication auth, Model model) {
        Notes note = notesService.getNoteById(id, currentUser(auth));
        note.getAttachments().size();
        model.addAttribute("note", note);
        return "note-view";
    }

    @GetMapping("/notes/edit/{id}")
    public String formEdit(@PathVariable Long id, Authentication auth, Model model) {
        Notes note = notesService.getNoteById(id, currentUser(auth));
        NoteRequest request = new NoteRequest();
        request.setTitle(note.getTitle());
        request.setContent(note.getContent());
        model.addAttribute("noteRequest", request);
        model.addAttribute("noteId", id);
        return "note-form";
    }

    @PostMapping("/notes/edit/{id}")
    public String edit(@PathVariable Long id,
                       @Valid @ModelAttribute("noteRequest") NoteRequest request,
                       BindingResult bindingResult,
                       Authentication auth,
                       Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("noteId", id);
            return "note-form";
        }
        notesService.updateNote(id, request, currentUser(auth));
        return "redirect:/notes";
    }

    @PostMapping("/notes/delete/{id}")
    public String toTrash(@PathVariable Long id, Authentication auth) {
        notesService.moveToTrash(id, currentUser(auth));
        return "redirect:/notes";
    }

    @PostMapping("/trash/restore/{id}")
    public String restore(@PathVariable Long id, Authentication auth) {
        notesService.restoreFromTrash(id, currentUser(auth));
        return "redirect:/trash";
    }

    @PostMapping("/trash/delete/{id}")
    public String delete(@PathVariable Long id, Authentication auth) {
        notesService.deletePermanently(id, currentUser(auth));
        return "redirect:/trash";
    }

    @PostMapping("/trash/clear")
    public String clearTrash(Authentication auth) {
        notesService.clearTrash(currentUser(auth));
        return "redirect:/trash";
    }

    @PostMapping("/notes/{id}/files")
    public String uploadFile(@PathVariable Long id,
                             @RequestParam("file") MultipartFile[] files,
                             Authentication auth) throws IOException {
        Notes note = notesService.getNoteById(id, currentUser(auth));
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                attachmentService.save(file, note);
            }
        }
        return "redirect:/notes/" + id;
    }

    @GetMapping("/notes/{id}/files/{fileId}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable Long id,
                                               @PathVariable Long fileId,
                                               Authentication auth) {
        Notes note = notesService.getNoteById(id, currentUser(auth));
        Attachment attachment = attachmentService.getById(fileId, note);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(
                attachment.getContentType() == null
                        ? MediaType.APPLICATION_OCTET_STREAM_VALUE
                        : attachment.getContentType()));
        headers.setContentDispositionFormData("attachment", attachment.getFileName());
        headers.setContentLength(attachment.getSize());

        return new ResponseEntity<>(attachment.getData(), headers, HttpStatus.OK);
    }

    @PostMapping("/notes/{id}/files/{fileId}/delete")
    public String deleteFile(@PathVariable Long id,
                             @PathVariable Long fileId,
                             Authentication auth) {
        Notes note = notesService.getNoteById(id, currentUser(auth));
        attachmentService.delete(fileId, note);
        return "redirect:/notes/" + id;
    }
}
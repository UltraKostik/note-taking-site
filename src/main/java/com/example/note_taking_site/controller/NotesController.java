package com.example.note_taking_site.controller;

import com.example.note_taking_site.dto.NoteRequest;
import com.example.note_taking_site.model.Attachment;
import com.example.note_taking_site.model.Notes;
import com.example.note_taking_site.model.User;
import com.example.note_taking_site.repository.UserRepository;
import com.example.note_taking_site.service.AttachmentService;
import com.example.note_taking_site.service.NotesService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;

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
        if (auth == null) {
            throw new IllegalStateException("User not authenticated");
        }
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found in DB"));
    }

    @GetMapping({"/", "/notes"})
    public String listActive(Authentication auth, Model model) {
        try {
            User user = currentUser(auth);
            model.addAttribute("notes", notesService.getActiveNotes(user));
            return "index";
        } catch (Exception e) {
            return "redirect:/login";
        }
    }

    @GetMapping("/trash")
    public String listTrash(Authentication auth, Model model) {
        try {
            User user = currentUser(auth);
            model.addAttribute("deletedNotes", notesService.getDeletedNotes(user));
            return "trash";
        } catch (Exception e) {
            return "redirect:/login";
        }
    }

    @GetMapping("/notes/add")
    public String formAdd(Model model) {
        model.addAttribute("noteRequest", new NoteRequest());
        return "note-form";
    }

    @GetMapping("/notes/edit/{id}")
    public String formEdit(@PathVariable Long id, Authentication auth, Model model) {
        try {
            NoteRequest request = new NoteRequest();
            Notes note = notesService.getNoteById(id, currentUser(auth));
            request.setTitle(note.getTitle());
            request.setContent(note.getContent());

            model.addAttribute("note", note);
            model.addAttribute("noteRequest", request);
            model.addAttribute("noteId", id);
            return "note-form";
        } catch (Exception e) {
            return "redirect:/notes";
        }
    }

    @PostMapping("/notes/add")
    public String add(@Valid @ModelAttribute("noteRequest") NoteRequest request,
                      BindingResult bindingResult,
                      @RequestParam(value = "file", required = false) MultipartFile[] files,
                      Authentication auth) {
        if (bindingResult.hasErrors()) {
            return "note-form";
        }

        User user = currentUser(auth);
        Notes note = notesService.createNote(request, user);

        if (files != null) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    try {
                        attachmentService.save(file, note);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return "redirect:/notes/edit/" + note.getId();
    }

    @GetMapping("/notes/{id}")
    public String view(@PathVariable Long id, Authentication auth) {
        try {
            notesService.getNoteById(id, currentUser(auth));
            return "redirect:/notes/edit/" + id;
        } catch (Exception e) {
            return "redirect:/notes";
        }
    }

    @PostMapping("/notes/edit/{id}")
    public String edit(@PathVariable Long id,
                       @Valid @ModelAttribute("noteRequest") NoteRequest request,
                       BindingResult bindingResult,
                       Authentication auth,
                       Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("noteId", id);
            try {
                model.addAttribute("note", notesService.getNoteById(id, currentUser(auth)));
            } catch (Exception ignored) {}
            return "note-form";
        }
        try {
            notesService.updateNote(id, request, currentUser(auth));
        } catch (Exception e) {
            return "redirect:/notes";
        }
        return "redirect:/notes";
    }

    @PostMapping("/notes/delete/{id}")
    public String toTrash(@PathVariable Long id, Authentication auth) {
        try {
            notesService.moveToTrash(id, currentUser(auth));
        } catch (Exception ignored) {}
        return "redirect:/notes";
    }

    @PostMapping("/trash/restore/{id}")
    public String restore(@PathVariable Long id, Authentication auth) {
        try {
            notesService.restoreFromTrash(id, currentUser(auth));
        } catch (Exception ignored) {}
        return "redirect:/trash";
    }

    @PostMapping("/trash/delete/{id}")
    public String delete(@PathVariable Long id, Authentication auth) {
        try {
            notesService.deletePermanently(id, currentUser(auth));
        } catch (Exception ignored) {}
        return "redirect:/trash";
    }

    @PostMapping("/trash/clear")
    public String clearTrash(Authentication auth) {
        try {
            notesService.clearTrash(currentUser(auth));
        } catch (Exception ignored) {}
        return "redirect:/trash";
    }

    @PostMapping("/notes/{id}/files")
    public String uploadFile(@PathVariable Long id,
                             @RequestParam("file") MultipartFile[] files,
                             Authentication auth) {
        try {
            Notes note = notesService.getNoteById(id, currentUser(auth));
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    attachmentService.save(file, note);
                }
            }
        } catch (Exception e) {
            return "redirect:/notes";
        }
        return "redirect:/notes/edit/" + id;
    }

    @GetMapping("/notes/{id}/files/{fileId}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable Long id,
                                               @PathVariable Long fileId,
                                               Authentication auth) {
        try {
            Notes note = notesService.getNoteById(id, currentUser(auth));
            Attachment attachment = attachmentService.getById(fileId, note);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(
                    attachment.getContentType() == null
                            ? MediaType.APPLICATION_OCTET_STREAM_VALUE
                            : attachment.getContentType()));
            headers.setContentDisposition(ContentDisposition.attachment()
                    .filename(attachment.getFileName(), StandardCharsets.UTF_8)
                    .build());
            headers.setContentLength(attachment.getSize());

            return new ResponseEntity<>(attachment.getData(), headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/notes/{id}/files/{fileId}/delete")
    public String deleteFile(@PathVariable Long id,
                             @PathVariable Long fileId,
                             Authentication auth) {
        try {
            Notes note = notesService.getNoteById(id, currentUser(auth));
            attachmentService.delete(fileId, note);
        } catch (Exception ignored) {}
        return "redirect:/notes/edit/" + id;
    }
}
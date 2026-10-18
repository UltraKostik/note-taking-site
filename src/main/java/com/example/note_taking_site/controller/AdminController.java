package com.example.note_taking_site.controller;

import com.example.note_taking_site.dto.NoteRequest;
import com.example.note_taking_site.model.Notes;
import com.example.note_taking_site.model.User;
import com.example.note_taking_site.service.NotesService;
import com.example.note_taking_site.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);

    private final UserService userService;
    private final NotesService notesService;

    public AdminController(UserService userService, NotesService notesService) {
        this.userService = userService;
        this.notesService = notesService;
    }

    @GetMapping
    public String listUsers(Model model) {
        log.info("Admin requested user list");
        List<User> users = userService.getAllUsers();
        model.addAttribute("usersList", users);
        model.addAttribute("totalUsers", users.size());
        model.addAttribute("totalNotes", notesService.getAllNotes().size());
        model.addAttribute("trashNotesCount",
                notesService.getAllByStatus(Notes.NoteStatus.DELETED).size());
        return "admin";
    }

    @GetMapping("/users/{id}/notes")
    public String userNotes(@PathVariable Long id, Model model) {
        try {
            User user = userService.getById(id);
            model.addAttribute("targetUser", user);
            model.addAttribute("notes", notesService.getNotesByAuthor(user));
            return "admin-user-notes";
        } catch (Exception e) {
            return "redirect:/admin";
        }
    }

    @PostMapping("/users/ban/{id}")
    public String banUser(@PathVariable Long id) {
        try {
            log.info("Admin banning user id={}", id);
            userService.deleteUser(id);
        } catch (Exception ignored) {}
        return "redirect:/admin";
    }

    @PostMapping("/users/{id}/role")
    public String changeRole(@PathVariable Long id, @RequestParam User.Role role) {
        try {
            log.info("Admin changing role of user id={} to {}", id, role);
            userService.changeRole(id, role);
        } catch (Exception ignored) {}
        return "redirect:/admin";
    }

    @GetMapping("/notes")
    public String listAllNotes(@RequestParam(required = false) Notes.NoteStatus status,
                               Model model) {
        log.info("Admin requested all notes, filter status={}", status);
        if (status == null) {
            model.addAttribute("notes", notesService.getAllNotes());
        } else {
            model.addAttribute("notes", notesService.getAllByStatus(status));
        }
        model.addAttribute("statuses", Notes.NoteStatus.values());
        model.addAttribute("selectedStatus", status);
        return "admin-notes";
    }

    @PostMapping("/notes/{id}/delete")
    public String deleteNote(@PathVariable Long id, @RequestParam(required = false) Long userId) {
        try {
            log.info("Admin deleting note id={}", id);
            notesService.deletePermanentlyAdmin(id);
        } catch (Exception ignored) {}

        if (userId != null) {
            return "redirect:/admin/users/" + userId + "/notes";
        }
        return "redirect:/admin/notes";
    }

    @GetMapping("/notes/{id}/edit")
    public String editNoteForm(@PathVariable Long id, Model model) {
        try {
            Notes note = notesService.getNoteByIdAdmin(id);
            NoteRequest request = new NoteRequest();
            request.setTitle(note.getTitle());
            request.setContent(note.getContent());
            model.addAttribute("noteRequest", request);
            model.addAttribute("noteId", id);
            model.addAttribute("isAdminEdit", true);
            model.addAttribute("userId", note.getAuthor().getId());
            return "note-form";
        } catch (Exception e) {
            return "redirect:/admin/notes";
        }
    }

    @PostMapping("/notes/{id}/edit")
    public String editNote(@PathVariable Long id,
                           @Valid @ModelAttribute("noteRequest") NoteRequest request,
                           BindingResult bindingResult,
                           @RequestParam(required = false) Long userId,
                           Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("noteId", id);
            model.addAttribute("isAdminEdit", true);
            model.addAttribute("userId", userId);
            return "note-form";
        }
        try {
            notesService.updateNoteAdmin(id, request);
        } catch (Exception ignored) {}

        if (userId != null) {
            return "redirect:/admin/users/" + userId + "/notes";
        }
        return "redirect:/admin/notes";
    }
}
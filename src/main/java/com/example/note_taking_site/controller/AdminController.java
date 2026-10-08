package com.example.note_taking_site.controller;

import com.example.note_taking_site.model.Notes;
import com.example.note_taking_site.model.User;
import com.example.note_taking_site.service.ProductService;
import com.example.note_taking_site.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);

    private final UserService userService;
    private final ProductService productService;

    public AdminController(UserService userService, ProductService productService) {
        this.userService = userService;
        this.productService = productService;
    }


    @GetMapping
    public String listUsers(Model model) {
        log.info("Admin requested user list");
        List<User> users = userService.getAllUsers();
        model.addAttribute("usersList", users);
        model.addAttribute("totalUsers", users.size());
        model.addAttribute("totalNotes", productService.getAllProducts().size());
        model.addAttribute("trashNotesCount",
                productService.getAllByStatus(Notes.NoteStatus.DELETED).size());
        return "admin";
    }

    @PostMapping("/users/ban/{id}")
    public String banUser(@PathVariable Long id) {
        log.info("Admin banning user id={}", id);
        userService.deleteUser(id);
        return "redirect:/admin";
    }

    @PostMapping("/users/{id}/role")
    public String changeRole(@PathVariable Long id, @RequestParam User.Role role) {
        log.info("Admin changing role of user id={} to {}", id, role);
        userService.changeRole(id, role);
        return "redirect:/admin";
    }


    @GetMapping("/notes")
    public String listAllNotes(@RequestParam(required = false) Notes.NoteStatus status,
                               Model model) {
        log.info("Admin requested all notes, filter status={}", status);
        if (status == null) {
            model.addAttribute("notes", productService.getAllProducts());
        } else {
            model.addAttribute("notes", productService.getAllByStatus(status));
        }
        model.addAttribute("statuses", Notes.NoteStatus.values());
        model.addAttribute("selectedStatus", status);
        return "admin-notes";
    }

    @PostMapping("/notes/{id}/delete")
    public String deleteNote(@PathVariable Long id) {
        log.info("Admin deleting note id={}", id);
        productService.deletePermanentlyAdmin(id);
        return "redirect:/admin/notes";
    }
}
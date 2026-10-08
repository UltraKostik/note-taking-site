package com.example.note_taking_site.controller;

import com.example.note_taking_site.model.Notes;
import com.example.note_taking_site.model.User;
import com.example.note_taking_site.service.ProductService;
import com.example.note_taking_site.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final ProductService productService;

    public AdminController(UserService userService, ProductService productService) {
        this.userService = userService;
        this.productService = productService;
    }

    @GetMapping("/users")
    public String listUsers(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "admin-users";
    }

    @PostMapping("/users/{id}/role")
    public String changeRole(@PathVariable Long id, @RequestParam User.Role role) {
        userService.changeRole(id, role);
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return "redirect:/admin/users";
    }

    @GetMapping("/notes")
    public String listAllNotes(@RequestParam(required = false) Notes.NoteStatus status,
                               Model model) {
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
        productService.deletePermanentlyAdmin(id);
        return "redirect:/admin/notes";
    }
}
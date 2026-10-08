package com.example.note_taking_site.controller;

import com.example.note_taking_site.dto.NoteRequest;
import com.example.note_taking_site.model.Notes;
import com.example.note_taking_site.model.User;
import com.example.note_taking_site.repository.UserRepository;
import com.example.note_taking_site.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class ProductController {

    private final ProductService productService;
    private final UserRepository userRepository;

    public ProductController(ProductService productService, UserRepository userRepository) {
        this.productService = productService;
        this.userRepository = userRepository;
    }

    private User currentUser(Authentication auth) {
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found in DB"));
    }

    @GetMapping("/notes")
    public String listActive(Authentication auth, Model model) {
        User user = currentUser(auth);
        model.addAttribute("notes", productService.getActiveNotes(user));
        return "products-list";
    }

    @GetMapping("/trash")
    public String listTrash(Authentication auth, Model model) {
        User user = currentUser(auth);
        model.addAttribute("deletedNotes", productService.getDeletedNotes(user));
        return "trash";
    }

    @GetMapping("/zametki/add")
    public String formAdd(Model model) {
        model.addAttribute("noteRequest", new NoteRequest());
        return "product-form";
    }

    @PostMapping("/zametki/add")
    public String add(@Valid @ModelAttribute("noteRequest") NoteRequest request,
                      BindingResult bindingResult,
                      Authentication auth) {
        if (bindingResult.hasErrors()) {
            return "product-form";
        }
        productService.createNote(request, currentUser(auth));
        return "redirect:/notes";
    }

    @GetMapping("/zametki/{id}")
    public String view(@PathVariable Long id, Authentication auth, Model model) {
        model.addAttribute("note", productService.getNoteById(id, currentUser(auth)));
        return "product-view";
    }

    @GetMapping("/zametki/edit/{id}")
    public String formEdit(@PathVariable Long id, Authentication auth, Model model) {
        Notes note = productService.getNoteById(id, currentUser(auth));
        NoteRequest request = new NoteRequest();
        request.setTitle(note.getTitle());
        request.setContent(note.getContent());
        model.addAttribute("noteRequest", request);
        model.addAttribute("noteId", id);
        return "product-form";
    }

    @PostMapping("/zametki/edit/{id}")
    public String edit(@PathVariable Long id,
                       @Valid @ModelAttribute("noteRequest") NoteRequest request,
                       BindingResult bindingResult,
                       Authentication auth,
                       Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("noteId", id);
            return "product-form";
        }
        productService.updateNote(id, request, currentUser(auth));
        return "redirect:/notes";
    }

    @PostMapping("/zametki/delete/{id}")
    public String toTrash(@PathVariable Long id, Authentication auth) {
        productService.moveToTrash(id, currentUser(auth));
        return "redirect:/notes";
    }

    @PostMapping("/trash/restore/{id}")
    public String restore(@PathVariable Long id, Authentication auth) {
        productService.restoreFromTrash(id, currentUser(auth));
        return "redirect:/trash";
    }

    @PostMapping("/trash/delete/{id}")
    public String delete(@PathVariable Long id, Authentication auth) {
        productService.deletePermanently(id, currentUser(auth));
        return "redirect:/trash";
    }

    @PostMapping("/trash/clear")
    public String clearTrash(Authentication auth) {
        productService.clearTrash(currentUser(auth));
        return "redirect:/trash";
    }
}
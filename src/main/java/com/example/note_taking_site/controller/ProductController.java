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
@RequestMapping("/products")
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

    @GetMapping
    public String listActive(Authentication auth, Model model) {
        User user = currentUser(auth);
        model.addAttribute("products", productService.getActiveNotes(user));
        return "products-list";
    }

    @GetMapping("/drafts")
    public String listDrafts(Authentication auth, Model model) {
        User user = currentUser(auth);
        model.addAttribute("products", productService.getDraftNotes(user));
        return "products-list";
    }

    @GetMapping("/trash")
    public String listTrash(Authentication auth, Model model) {
        User user = currentUser(auth);
        model.addAttribute("products", productService.getDeletedNotes(user));
        return "products-list";
    }

    @GetMapping("/add")
    public String formAdd(Model model) {
        model.addAttribute("noteRequest", new NoteRequest());
        return "product-form";
    }

    @PostMapping("/add")
    public String add(@Valid @ModelAttribute("noteRequest") NoteRequest request,
                      BindingResult bindingResult,
                      Authentication auth) {
        if (bindingResult.hasErrors()) {
            return "product-form";
        }
        productService.createNote(request, currentUser(auth));
        return "redirect:/products";
    }

    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Authentication auth, Model model) {
        model.addAttribute("note", productService.getNoteById(id, currentUser(auth)));
        return "product-view";
    }

    @GetMapping("/{id}/edit")
    public String formEdit(@PathVariable Long id, Authentication auth, Model model) {
        Notes note = productService.getNoteById(id, currentUser(auth));
        NoteRequest request = new NoteRequest();
        request.setName(note.getName());
        request.setDescription(note.getDescription());
        model.addAttribute("noteRequest", request);
        model.addAttribute("noteId", id);
        return "product-form";
    }

    @PostMapping("/{id}/edit")
    public String edit(@PathVariable Long id,
                       @Valid @ModelAttribute("noteRequest") NoteRequest request,
                       BindingResult bindingResult,
                       Authentication auth) {
        if (bindingResult.hasErrors()) {
            return "product-form";
        }
        productService.updateNote(id, request, currentUser(auth));
        return "redirect:/products";
    }

    @PostMapping("/{id}/trash")
    public String toTrash(@PathVariable Long id, Authentication auth) {
        productService.moveToTrash(id, currentUser(auth));
        return "redirect:/products";
    }

    @PostMapping("/{id}/restore")
    public String restore(@PathVariable Long id, Authentication auth) {
        productService.restoreFromTrash(id, currentUser(auth));
        return "redirect:/products/trash";
    }

    @PostMapping("/{id}/draft")
    public String toDraft(@PathVariable Long id, Authentication auth) {
        productService.moveToDraft(id, currentUser(auth));
        return "redirect:/products";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, Authentication auth) {
        productService.deletePermanently(id, currentUser(auth));
        return "redirect:/products/trash";
    }
}
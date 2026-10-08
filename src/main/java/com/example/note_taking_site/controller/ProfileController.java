package com.example.note_taking_site.controller;

import com.example.note_taking_site.model.User;
import com.example.note_taking_site.repository.UserRepository;
import com.example.note_taking_site.service.ProductService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProfileController {

    private final UserRepository userRepository;
    private final ProductService productService;

    public ProfileController(UserRepository userRepository, ProductService productService) {
        this.userRepository = userRepository;
        this.productService = productService;
    }

    @GetMapping("/profile")
    public String profile(Authentication auth, Model model) {
        User user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found in DB"));
        model.addAttribute("user", user);
        model.addAttribute("zametki", productService.getActiveNotes(user));
        return "profile";
    }
}
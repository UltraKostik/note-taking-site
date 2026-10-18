package com.example.note_taking_site.controller;

import com.example.note_taking_site.model.Notes;
import com.example.note_taking_site.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public String listProducts(Model model) {
        model.addAttribute("products", productService.getAllProducts());
        return "products-list";
    }

    @GetMapping("/add")
    public String formAddProduct(Model model) {
        model.addAttribute("product", new Notes());
        return "product-form";
    }

    @PostMapping("/add")
    public String addProduct(@ModelAttribute("product") Notes product) {
        productService.saveProduct(product);
        return "redirect:/products";
    }
}
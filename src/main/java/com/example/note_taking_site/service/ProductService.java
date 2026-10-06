package com.example.note_taking_site.service;

import com.example.note_taking_site.model.Notes;
import com.example.note_taking_site.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public void saveProduct(Notes product) {
        productRepository.save(product);
    }

    public List<Notes> getAllProducts() {
        return productRepository.findAll();
    }
}
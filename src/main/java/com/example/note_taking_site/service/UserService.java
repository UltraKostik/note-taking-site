package com.example.note_taking_site.service;

import com.example.note_taking_site.dto.RegisterRequest;
import com.example.note_taking_site.exception.UserAlreadyExistsException;
import com.example.note_taking_site.model.User;
import com.example.note_taking_site.repository.ProductRepository;
import com.example.note_taking_site.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       ProductRepository productRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException(request.getEmail());
        }
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(User.Role.USER);
        log.info("Registering new user with email {}", request.getEmail());
        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
    }

    public void changeRole(Long id, User.Role role) {
        User user = getById(id);
        user.setRole(role);
        userRepository.save(user);
        log.info("Changing role of user id={} to {}", id, role);
    }

    public void deleteUser(Long id) {
        User user = getById(id);
        productRepository.deleteAllByAuthor(user);
        userRepository.delete(user);
        log.info("Deleting user id={} and all their notes", id);
    }
}